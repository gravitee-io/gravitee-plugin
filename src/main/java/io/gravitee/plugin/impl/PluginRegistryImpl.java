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
package io.gravitee.plugin.impl;

import io.gravitee.plugin.*;
import io.gravitee.plugin.utils.FileUtils;
import io.gravitee.plugin.utils.GlobMatchingFileVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @author David BRASSELY (brasseld at gmail.com)
 */
public class PluginRegistryImpl implements PluginRegistry {

    private final Logger LOGGER = LoggerFactory.getLogger(PluginRegistryImpl.class);

    private final static String JAR_EXTENSION = ".jar";
    private final static String JAR_GLOB = '*' + JAR_EXTENSION;

    private final static String PLUGIN_MANIFEST_FILE = "plugin.properties";

    @Value("${plugins.registry.path}")
    private String workspacePath;

    private boolean initialized = false;

    @Autowired
    private PluginLoader pluginLoader;

    /**
     * Empty constructor is used to use a workspace directory defined from @Value annotation
     * on workspacePath field.
     */
    public PluginRegistryImpl() {
    }

    public PluginRegistryImpl(String workspacePath) {
        this.workspacePath = workspacePath;
    }

    @Override
    public void init() {
        if (! initialized) {
            LOGGER.info("Initializing plugin registry.");
            this.init0();
            LOGGER.info("Initializing plugin registry. DONE");
        } else {
            LOGGER.warn("Plugin registry has already been initialized.");
        }
    }

    private void init0() {
        if (workspacePath == null || workspacePath.isEmpty()) {
            LOGGER.error("Plugin registry path is not specified.");
            throw new RuntimeException("Plugin registry path is not specified.");
        }

        File workspaceDir = new File(workspacePath);

        // Quick sanity check on the install root
        if (! workspaceDir.isDirectory()) {
            LOGGER.error("Invalid registry directory, {} is not a directory.", workspaceDir.getAbsolutePath());
            throw new RuntimeException("Invalid registry directory. Not a directory: "
                    + workspaceDir.getAbsolutePath());
        }

        LOGGER.info("Loading plugins from {}", workspaceDir.getAbsoluteFile());
        List<File> subdirectories = getChildren(workspaceDir.getAbsolutePath());

        LOGGER.info("\t{} plugin directories have been found.", subdirectories.size());
        for(File pluginDir: subdirectories) {
            loadPluginDirectory(pluginDir.getAbsolutePath());
        }

        initialized = true;
    }

    /**
     * Load a plugin from a directory.
     *
     * Plugin structure in the workspace is as follow:
     *  my-plugin-dir/
     *      my-plugin.jar
     *      lib/
     *          dependency01.jar
     *          dependency02.jar
     *
     * @param pluginDir The directory containing the plugin definition
     */
    private void loadPluginDirectory(String pluginDir) {
        Path pluginDirPath = FileSystems.getDefault().getPath(pluginDir);
        LOGGER.info("Trying to load plugin from {}", pluginDirPath);

        PluginManifest manifest = readPluginManifest(pluginDirPath);
        if (manifest != null) {
            URL [] dependencies = extractPluginDependencies(pluginDirPath);
            pluginLoader.load(new PluginContext() {
                @Override
                public String id() {
                    return manifest.id();
                }

                @Override
                public Path path() {
                    return pluginDirPath;
                }

                @Override
                public PluginManifest manifest() {
                    return manifest;
                }

                @Override
                public URL[] dependencies() {
                    return dependencies;
                }
            });
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
                LOGGER.debug("Unable to found a jar in the root directory: {}", pluginPath);
                return null;
            }

            Path pluginJarPath = (Path) iterator.next();
            LOGGER.debug("Found a jar in the root directory, looking for a plugin manifest in {}", pluginJarPath);

            Properties pluginManifestProperties = loadPluginManifest(pluginJarPath.toString());
            if (pluginManifestProperties == null) {
                LOGGER.error("No plugin.properties can be found from {}", pluginJarPath);
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
        };
    }

    private List<File> getChildren(String directory) {
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
}
