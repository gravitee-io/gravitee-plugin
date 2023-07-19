/*
 * Copyright © 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.plugin.core.internal;

import io.gravitee.common.event.EventManager;
import io.gravitee.common.service.AbstractService;
import io.gravitee.plugin.core.api.*;
import io.gravitee.plugin.core.utils.FileUtils;
import io.gravitee.plugin.core.utils.GlobMatchingFileVisitor;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@Slf4j
@RequiredArgsConstructor
public class PluginRegistryImpl extends AbstractService<PluginRegistry> implements PluginRegistry {

    public static final String PROPERTY_STRING_FORMAT = "%s.%s.enabled";

    private static final String JAR_EXTENSION = ".jar";
    private static final String JAR_GLOB = '*' + JAR_EXTENSION;

    private static final String ZIP_EXTENSION = ".zip";
    private static final String ZIP_GLOB = '*' + ZIP_EXTENSION;

    private static final String PLUGIN_MANIFEST_FILE = "plugin.properties";

    private static final Map<String, String> PLUGIN_TYPE_PROPERTY_ALIASES = new HashMap<>();

    static {
        PLUGIN_TYPE_PROPERTY_ALIASES.put("service", "services");
        PLUGIN_TYPE_PROPERTY_ALIASES.put("policy", "policies");
        PLUGIN_TYPE_PROPERTY_ALIASES.put("alert", "alerts");
        PLUGIN_TYPE_PROPERTY_ALIASES.put("fetcher", "fetchers");
        PLUGIN_TYPE_PROPERTY_ALIASES.put("connector", "connectors");
        PLUGIN_TYPE_PROPERTY_ALIASES.put("notifier", "notifiers");
        PLUGIN_TYPE_PROPERTY_ALIASES.put("service_discovery", "service-discoveries");
    }

    private final PluginRegistryConfiguration configuration;

    private final Environment environment;

    private final ExecutorService executor;

    private final EventManager eventManager;

    private boolean init = false;

    private final List<Plugin> plugins = new ArrayList<>();

    private String[] workspacesPath;

    public void setWorkspacesPath(String workspacePath) {
        this.workspacesPath = new String[] { workspacePath };
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        if (!init) {
            log.info("Initializing plugin registry.");
            this.init();
            log.info("Plugins have been loaded and installed.");
        } else {
            log.warn("Plugin registry has already been initialized.");
        }
    }

    public void init() throws Exception {
        String[] pluginsPath = configuration.getPluginsPath();
        if ((pluginsPath == null || pluginsPath.length == 0) && workspacesPath == null) {
            log.error("No plugin registry configured.");
            throw new IllegalArgumentException("No plugin registry configured.");
        }

        // Use override configuration
        if (workspacesPath != null) {
            pluginsPath = workspacesPath;
        }

        this.plugins.addAll(
                Flowable
                    .fromArray(pluginsPath)
                    .doOnSubscribe(s -> this.init = true)
                    .flatMap(this::loadPluginsFromPath)
                    // reserve sort
                    .sorted((p1, p2) -> Math.negateExact(((Long) p1.getArchiveTimestamp()).compareTo(p2.getArchiveTimestamp())))
                    // As plugins arrive sorted by reverse file date
                    // we can exclude duplicates and keep the most recent one
                    .distinct()
                    .doOnNext(plugin -> eventManager.publishEvent(PluginEvent.DEPLOYED, plugin))
                    .cast(Plugin.class)
                    .toList()
                    .doOnSuccess(PluginRegistryImpl::printPlugins)
                    .blockingGet()
            );

        // Publish the ENDED event when the plugins list is ready
        eventManager.publishEvent(PluginEvent.ENDED, null);
    }

    private Flowable<PluginImpl> loadPluginsFromPath(final String pluginPathAsString) throws IOException {
        File pluginDir = new File(pluginPathAsString);

        // Quick sanity check
        if (!pluginDir.isDirectory()) {
            return Flowable.error(
                new IllegalArgumentException("Invalid registry directory. Not a directory: " + pluginDir.getAbsolutePath())
            );
        }

        final Path pluginPath = pluginDir.toPath();
        log.info("Loading plugins from {}", pluginDir);

        final DirectoryStream<Path> stream = FileUtils.newDirectoryStream(pluginPath, ZIP_GLOB);
        return Flowable
            .fromIterable(stream)
            .subscribeOn(Schedulers.from(executor))
            .map(path -> loadPlugin(pluginDir, path))
            .filter(PluginImpl::valid)
            .doFinally(stream::close);
    }

    private static void printPlugins(final List<Plugin> plugins) {
        plugins.stream().map(Plugin::type).distinct().forEach(type -> printPluginByType(plugins, type));
    }

    private static void printPluginByType(final List<Plugin> plugins, final String pluginType) {
        log.info("List of available {}: ", pluginType.toLowerCase());
        plugins
            .stream()
            .filter(plugin -> pluginType.equalsIgnoreCase(plugin.type()))
            .forEach(plugin -> log.info("\t> {} [{}] has been loaded", plugin.id(), plugin.manifest().version()));
    }

    /**
     * Load a plugin from a zip archive.
     * <p>
     * Plugin archive structure must be as follows:
     * <pre>
     *  my-plugin-dir/
     *      my-plugin.jar
     *      lib/
     *          dependency01.jar
     *          dependency02.jar
     * </pre>
     *
     * @param registryDir       The directory containing plugins
     * @param pluginArchivePath The directory containing the plugin definition
     */
    private PluginImpl loadPlugin(File registryDir, Path pluginArchivePath) {
        log.debug("Loading plugin archive {}", pluginArchivePath);

        // create an invalid (empty) plugin as RxJava do not support null values.
        PluginImpl plugin = new PluginImpl(PluginManifestFactory.create(new Properties()));

        try {
            // 1_ Extract plugin into a temporary working folder
            String sPluginFile = pluginArchivePath.toFile().getName();
            sPluginFile = sPluginFile.substring(0, sPluginFile.lastIndexOf(".zip"));
            Path workDir = FileSystems.getDefault().getPath(registryDir.getAbsolutePath(), ".work", sPluginFile);
            if (StringUtils.hasText(configuration.getPluginWorkDir())) {
                // use specified workDir if specified in environment
                workDir = FileSystems.getDefault().getPath(configuration.getPluginWorkDir(), sPluginFile);
                // make sure the work dir exists
                if (!workDir.toFile().getParentFile().exists()) {
                    workDir.toFile().getParentFile().mkdirs();
                }
            }

            FileUtils.delete(workDir);
            FileUtils.unzip(pluginArchivePath.toString(), workDir);

            // 2_ Load plugin from the working folder
            PluginManifest manifest = readPluginManifest(workDir);
            if (manifest != null && isEnabled(manifest)) {
                URL[] pluginDependencies = extractPluginDependencies(workDir);
                URL[] extDependencies = extractPluginExtensionDependencies(manifest, registryDir.toPath());

                URL[] dependencies = Arrays.copyOf(pluginDependencies, pluginDependencies.length + extDependencies.length);
                System.arraycopy(extDependencies, 0, dependencies, pluginDependencies.length, extDependencies.length);

                plugin = new PluginImpl(manifest);
                plugin.setArchiveTimestamp(getFileTimestamp(pluginArchivePath));
                plugin.setPath(workDir);
                plugin.setDependencies(dependencies);
            }
        } catch (IOException ioe) {
            log.error("An unexpected error occurs while loading plugin archive {}", pluginArchivePath, ioe);
        }
        return plugin;
    }

    static long getFileTimestamp(Path pluginArchivePath) throws IOException {
        return Files.getLastModifiedTime(pluginArchivePath).toInstant().toEpochMilli();
    }

    /**
     * Check if plugin is enabled.
     * First, check in {@link PluginRegistryImpl#PLUGIN_TYPE_PROPERTY_ALIASES} plugin type has an alias for properties.
     * Alias matches the implementation of {@link AbstractPluginHandler#type()}: plugin.type will be "service" but pluginHandler.type will be "services".
     * If property is not contained based on alias, do a regular search of the property.
     *
     * @param pluginManifest the plugin manifest object
     * @return true if plugin is enabled
     */
    private boolean isEnabled(PluginManifest pluginManifest) {
        boolean enabled;
        final String propertyFromAlias = String.format(
            PROPERTY_STRING_FORMAT,
            PLUGIN_TYPE_PROPERTY_ALIASES.get(pluginManifest.type()),
            pluginManifest.id()
        );
        if (PLUGIN_TYPE_PROPERTY_ALIASES.containsKey(pluginManifest.type()) && environment.containsProperty(propertyFromAlias)) {
            enabled = environment.getProperty(propertyFromAlias, Boolean.class, true);
        } else {
            enabled =
                environment.getProperty(
                    String.format(PROPERTY_STRING_FORMAT, pluginManifest.type(), pluginManifest.id()),
                    Boolean.class,
                    true
                );
        }
        log.debug("Plugin {} is loaded in registry: {}", pluginManifest.id(), enabled);
        return enabled;
    }

    /**
     * Extract plugin dependencies by reading all jars from plugin directory root path.
     *
     * @param pluginDirPath Plugin directory root path
     * @return Plugin ext dependency URLs or empty array (if an error occurres)
     */
    private URL[] extractPluginDependencies(Path pluginDirPath) {
        try {
            GlobMatchingFileVisitor visitor = new GlobMatchingFileVisitor(JAR_GLOB);
            Files.walkFileTree(pluginDirPath, visitor);
            List<Path> pluginDependencies = visitor.getMatchedPaths();
            return pathsToURLArray(pluginDependencies);
        } catch (IOException ioe) {
            log.error("Unexpected error while looking for plugin dependencies", ioe);
            return new URL[0];
        }
    }

    /**
     * Extract plugin dependency from ext directory to extend easily plugin classloader
     *
     * @param manifest     Plugin manifest
     * @param registryPath Path to the plugin registry
     * @return Plugin ext dependency URLs or empty array (if an error occurres)
     */
    private URL[] extractPluginExtensionDependencies(PluginManifest manifest, Path registryPath) {
        Path extPath = Paths.get(registryPath.toString(), "ext", manifest.id());
        if (extPath.toFile().exists()) {
            return extractPluginDependencies(extPath);
        } else {
            return new URL[0];
        }
    }

    private PluginManifest readPluginManifest(Path pluginPath) {
        try (DirectoryStream<Path> stream = FileUtils.newDirectoryStream(pluginPath, JAR_GLOB)) {
            Iterator<Path> iterator = stream.iterator();
            if (!iterator.hasNext()) {
                log.debug("Unable to find a jar in the root directory: {}", pluginPath);
                return null;
            }

            Path pluginJarPath = iterator.next();
            log.debug("Found a jar in the root directory, looking for a plugin manifest in {}", pluginJarPath);

            Properties pluginManifestProperties = loadPluginManifest(pluginJarPath.toString());
            if (pluginManifestProperties.isEmpty()) {
                log.error("No plugin.properties found from {}", pluginJarPath);
                return null;
            }

            log.debug("A plugin manifest has been loaded from: {}", pluginJarPath);

            PluginManifestValidator validator = new PropertiesBasedPluginManifestValidator(pluginManifestProperties);
            if (!validator.validate()) {
                log.error("Plugin manifest not valid, skipping plugin registration.");
                return null;
            }

            return PluginManifestFactory.create(pluginManifestProperties);
        } catch (IOException ioe) {
            log.error("Unexpected error while trying to load plugin manifest", ioe);
            throw new IllegalStateException("Unexpected error while trying to load plugin manifest", ioe);
        }
    }

    private Properties loadPluginManifest(String pluginPath) {
        try (FileSystem zipFileSystem = FileUtils.createZipFileSystem(pluginPath, false)) {
            final Path root = zipFileSystem.getPath("/");

            // Walk the jar file tree and search for plugin.properties file
            PluginManifestVisitor visitor = new PluginManifestVisitor();
            Files.walkFileTree(root, visitor);
            Path pluginManifestPath = visitor.getPluginManifest();

            if (pluginManifestPath != null) {
                Properties properties = new Properties();
                try (InputStream is = Files.newInputStream(pluginManifestPath)) {
                    properties.load(is);
                }
                return properties;
            }
        } catch (IOException e) {
            log.error("{}", e.getMessage(), e);
        }

        return new Properties();
    }

    private URL[] pathsToURLArray(List<Path> paths) {
        URL[] urls = new URL[paths.size()];
        int idx = 0;

        for (Path path : paths) {
            try {
                urls[idx++] = path.toUri().toURL();
            } catch (IOException ioe) {
                // path are coming from FS so they should be OK
            }
        }

        return urls;
    }

    @Override
    public Collection<Plugin> plugins() {
        return plugins;
    }

    @Override
    public Collection<Plugin> plugins(String type) {
        return plugins.stream().filter(pluginContext -> type.equalsIgnoreCase(pluginContext.type())).collect(Collectors.toSet());
    }

    static class PluginManifestVisitor extends SimpleFileVisitor<Path> {

        private Path pluginManifest = null;

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (file.getFileName().toString().equals(PLUGIN_MANIFEST_FILE)) {
                pluginManifest = file;
                return FileVisitResult.TERMINATE;
            }

            return super.visitFile(file, attrs);
        }

        public Path getPluginManifest() {
            return pluginManifest;
        }
    }
}
