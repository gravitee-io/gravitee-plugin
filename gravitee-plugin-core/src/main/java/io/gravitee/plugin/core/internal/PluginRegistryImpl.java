/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class PluginRegistryImpl extends AbstractService implements PluginRegistry {

    private final Logger LOGGER = LoggerFactory.getLogger(PluginRegistryImpl.class);

    private final static String JAR_EXTENSION = ".jar";
    private final static String JAR_GLOB = '*' + JAR_EXTENSION;

    private final static String ZIP_EXTENSION = ".zip";
    private final static String ZIP_GLOB = '*' + ZIP_EXTENSION;

    private final static String PLUGIN_MANIFEST_FILE = "plugin.properties";

    @Autowired
    private PluginRegistryConfiguration configuration;

    private boolean init = false;

    private List<Plugin> plugins = new ArrayList<>();

    @Autowired
    private EventManager eventManager;

    private String [] workspacesPath;

    /**
     * Empty constructor is used to use a workspace directory defined from @Value annotation
     * on workspacePath field.
     */
    public PluginRegistryImpl() {
    }

    public PluginRegistryImpl(String workspacePath) {
        this.workspacesPath = new String []{ workspacePath };
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        if (!init) {
            LOGGER.info("Initializing plugin registry.");
            this.init();
            LOGGER.info("Plugins have been loaded and installed.");
        } else {
            LOGGER.warn("Plugin registry has already been initialized.");
        }
    }

    public void init() throws Exception {
        String [] pluginsPath = configuration.getPluginsPath();
        if ((pluginsPath == null || pluginsPath.length == 0) && workspacesPath == null) {
            LOGGER.error("No plugin registry configured.");
            throw new RuntimeException("No plugin registry configured.");
        }

        // Use override configuration
        if (workspacesPath != null) {
            pluginsPath = workspacesPath;
        }

        for (String aWorkspacePath : pluginsPath) {
            loadPluginsFromRegistry(aWorkspacePath);
        }

        printPlugins();
        eventManager.publishEvent(PluginEvent.ENDED, null);
    }

    private void loadPluginsFromRegistry(String registryPath) throws Exception {
        File registryDir = new File(registryPath);

        // Quick sanity check on the install root
        if (! registryDir.isDirectory()) {
            LOGGER.error("Invalid registry directory, {} is not a directory.", registryDir.getAbsolutePath());
            throw new RuntimeException("Invalid registry directory. Not a directory: "
                    + registryDir.getAbsolutePath());
        }

        loadPlugins(registryDir);
    }

    private void loadPlugins(File registryDir) throws Exception {
        Path registryPath = registryDir.toPath();
        LOGGER.info("Loading plugins from {}",registryDir);

        try {
            DirectoryStream<Path> stream = FileUtils.newDirectoryStream(registryPath, ZIP_GLOB);
            Iterator<Path> archiveIte = stream.iterator();

            if (archiveIte.hasNext()) {
                while (archiveIte.hasNext()) {
                    loadPlugin(registryDir, archiveIte.next());
                }
            } else {
                LOGGER.warn("No plugin has been found in {}", registryDir);
            }

            init = true;
        } catch (IOException ioe) {
            LOGGER.error("An unexpected error occurs", ioe);
            throw ioe;
        }
    }

    private void printPlugins() {
        for (PluginType pluginType : PluginType.values()) {
            printPluginByType(pluginType);
        }
    }

    private void printPluginByType(PluginType pluginType) {
        LOGGER.info("List of available {}: ", pluginType.name().toLowerCase());
        plugins.stream()
                .filter(plugin -> pluginType == plugin.type())
                .forEach(plugin -> LOGGER.info("\t> {} [{}] has been loaded",
                        plugin.id(), plugin.manifest().version()));
    }

    /**
     * Load a plugin from a zip archive.
     *
     * Plugin archive structure must be as follow:
     *  my-plugin-dir/
     *      my-plugin.jar
     *      lib/
     *          dependency01.jar
     *          dependency02.jar
     *
     * @param registryDir The directory containing plugins
     * @param pluginArchivePath The directory containing the plugin definition
     */
    private void loadPlugin(File registryDir, Path pluginArchivePath) {
        LOGGER.info("Loading plugin from {}", pluginArchivePath);

        try {
            // 1_ Extract plugin into a temporary working folder
            String sPluginFile = pluginArchivePath.toFile().getName();
            sPluginFile = sPluginFile.substring(0, sPluginFile.lastIndexOf(".zip"));
            Path workDir = FileSystems.getDefault().getPath(registryDir.getAbsolutePath(), ".work", sPluginFile);
            FileUtils.delete(workDir);

            FileUtils.unzip(pluginArchivePath.toString(), workDir);

            // 2_ Load plugin from the working folder
            PluginManifest manifest = readPluginManifest(workDir);
            if (manifest != null) {
                URL[] dependencies = extractPluginDependencies(workDir);

                PluginImpl plugin = new PluginImpl(manifest);
                plugin.setPath(workDir);
                plugin.setDependencies(dependencies);

                eventManager.publishEvent(PluginEvent.DEPLOYED, plugin);
                plugins.add(plugin);
            }
        } catch (IOException ioe) {
            LOGGER.error("An unexpected error occurs while loading plugin archive {}", pluginArchivePath, ioe);
        }
    }

    /**
     * Extract plugin dependencies by reading all jars from plugin directory root path.
     *
     * @param pluginDirPath Plugin directory root path
     * @return Plugin dependency URLs
     */
    private URL[] extractPluginDependencies(Path pluginDirPath) {
        try {
            GlobMatchingFileVisitor visitor = new GlobMatchingFileVisitor(JAR_GLOB);
            Files.walkFileTree(pluginDirPath, visitor);
            List<Path> pluginDependencies = visitor.getMatchedPaths();
            return listToArray(pluginDependencies);
        } catch (IOException ioe) {
            LOGGER.error("Unexpected error while looking for plugin dependencies", ioe);
            return null;
        }
    }

    /**
     *
     * @param pluginPath
     * @return
     */
    private PluginManifest readPluginManifest(Path pluginPath) {
        try {
            Iterator iterator = FileUtils.newDirectoryStream(pluginPath, JAR_GLOB).iterator();
            if (! iterator.hasNext()) {
                LOGGER.debug("Unable to find a jar in the root directory: {}", pluginPath);
                return null;
            }

            Path pluginJarPath = (Path) iterator.next();
            LOGGER.debug("Found a jar in the root directory, looking for a plugin manifest in {}", pluginJarPath);

            Properties pluginManifestProperties = loadPluginManifest(pluginJarPath.toString());
            if (pluginManifestProperties == null) {
                LOGGER.error("No plugin.properties found from {}", pluginJarPath);
                return null;
            }

            LOGGER.info("A plugin manifest has been loaded from: {}", pluginJarPath);

            PluginManifestValidator validator = new PropertiesBasedPluginManifestValidator(pluginManifestProperties);
            if (! validator.validate()) {
                LOGGER.error("Plugin manifest not valid, skipping plugin registration.");
                return null;
            }

            return create(pluginManifestProperties);
        } catch (IOException ioe) {
            LOGGER.error("Unexpected error while trying to load plugin manifest", ioe);
            throw new IllegalStateException("Unexpected error while trying to load plugin manifest", ioe);
        }
    }

    private Properties loadPluginManifest(String pluginPath) {
        try (FileSystem zipFileSystem = FileUtils.createZipFileSystem(pluginPath, false)){
            final Path root = zipFileSystem.getPath("/");

            // Walk the jar file tree and search for plugin.properties file
            PluginManifestVisitor visitor = new PluginManifestVisitor();
            Files.walkFileTree(root, visitor);
            Path pluginManifestPath = visitor.getPluginManifest();

            if (pluginManifestPath != null) {
                Properties properties = new Properties();
                properties.load(Files.newInputStream(pluginManifestPath));

                return properties;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private URL[] listToArray(List<Path> paths) {
        URL [] urls = new URL[paths.size()];
        int idx = 0;

        for(Path path: paths) {
            try {
                urls[idx++] = path.toUri().toURL();
            } catch (IOException ioe) {}
        }

        return urls;
    }

    /**
     * Create a manifest from a properties file.
     *
     * @param properties The properties file to read.
     * @return A plugin manifest.
     */
    private PluginManifest create(Properties properties) {
        final String id = properties.getProperty(PluginManifestProperties.MANIFEST_ID_PROPERTY);
        final String description = properties.getProperty(PluginManifestProperties.MANIFEST_DESCRIPTION_PROPERTY);
        final String clazz = properties.getProperty(PluginManifestProperties.MANIFEST_CLASS_PROPERTY);
        final String name = properties.getProperty(PluginManifestProperties.MANIFEST_NAME_PROPERTY);
        final String version = properties.getProperty(PluginManifestProperties.MANIFEST_VERSION_PROPERTY);
        final String type = properties.getProperty(PluginManifestProperties.MANIFEST_TYPE_PROPERTY);

        return new PluginManifest() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public String description() {
                return description;
            }

            @Override
            public String version() {
                return version;
            }

            @Override
            public String plugin() {
                return clazz;
            }

            @Override
            public String type() {
                return type;
            }
        };
    }

    private List<File> getPluginsArchive(String directory) {
        DirectoryStream.Filter<Path> filter = file -> (Files.isDirectory(file));

        List<File> files = new ArrayList<>();
        Path dir = FileSystems.getDefault().getPath(directory);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir,
                filter)) {
            for (Path path : stream) {
                files.add(path.toFile());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return files;
    }

    @Override
    public Collection<Plugin> plugins() {
        return plugins;
    }

    @Override
    public Collection<Plugin> plugins(PluginType type) {
        return plugins.stream()
                .filter(pluginContext -> pluginContext.type() == type)
                .collect(Collectors.toSet());
    }

    public void setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    class PluginManifestVisitor extends SimpleFileVisitor<Path> {
        private Path pluginManifest = null;

        @Override
        public FileVisitResult visitFile(Path file,
                                         BasicFileAttributes attrs) throws IOException {
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

    public void setConfiguration(PluginRegistryConfiguration configuration) {
        this.configuration = configuration;
    }
}
