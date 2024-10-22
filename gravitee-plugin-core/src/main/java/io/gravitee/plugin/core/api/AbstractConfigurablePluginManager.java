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
package io.gravitee.plugin.core.api;

import io.gravitee.plugin.core.internal.PluginManifestProperties;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public abstract class AbstractConfigurablePluginManager<T extends ConfigurablePlugin>
    extends AbstractPluginManager<T>
    implements ConfigurablePluginManager<T> {

    private final Logger logger = LoggerFactory.getLogger(AbstractConfigurablePluginManager.class);

    private static final String SCHEMAS_DIRECTORY = "schemas";

    private static final String DOCS_DIRECTORY = "docs";

    @Override
    public String getSchema(String pluginId) throws IOException {
        return getSchema(pluginId, false);
    }

    @Override
    public String getSchema(String pluginId, boolean includeNotDeployed) throws IOException {
        final T plugin = get(pluginId, includeNotDeployed);
        String schemaProperty = plugin.manifest().properties().get(PluginManifestProperties.SCHEMA_PROPERTY);

        if (schemaProperty != null) {
            return getFile(pluginId, Paths.get(SCHEMAS_DIRECTORY, schemaProperty), includeNotDeployed);
        }

        return getFirstFile(pluginId, SCHEMAS_DIRECTORY, includeNotDeployed);
    }

    @Override
    public String getSchema(String pluginId, String subFolder) throws IOException {
        return getSchema(pluginId, subFolder, false);
    }

    @Override
    public String getSchema(String pluginId, String subFolder, boolean includeNotDeployed) throws IOException {
        return getFirstFile(pluginId, String.format("%s/%s", SCHEMAS_DIRECTORY, subFolder), includeNotDeployed);
    }

    @Override
    public String getSchema(String pluginId, String propertyKey, boolean fallbackToSchema, boolean includeNotDeployed) throws IOException {
        final T plugin = get(pluginId, includeNotDeployed);

        String schemaProperty = plugin.manifest().properties().get(propertyKey);
        if (schemaProperty != null) {
            return getFile(pluginId, Paths.get(SCHEMAS_DIRECTORY, schemaProperty), includeNotDeployed);
        }

        if (fallbackToSchema) {
            return getSchema(pluginId, includeNotDeployed);
        }
        return null;
    }

    @Override
    public String getIcon(String pluginId) throws IOException {
        return getIcon(pluginId, false);
    }

    @Override
    public String getIcon(String pluginId, boolean includeNotDeployed) throws IOException {
        T plugin = get(pluginId, includeNotDeployed);
        if (plugin != null) {
            Map<String, String> properties = plugin.manifest().properties();
            return this.getFileFromPropertyAsBase64(plugin, properties, PluginManifestProperties.MANIFEST_ICON_PROPERTY);
        }
        return null;
    }

    @Override
    public String getDocumentation(String pluginId) throws IOException {
        return this.getDocumentation(pluginId, false);
    }

    @Override
    public String getDocumentation(String pluginId, boolean includeNotDeployed) throws IOException {
        final T plugin = get(pluginId, includeNotDeployed);

        var documentationProperty = plugin.manifest().properties().get(PluginManifestProperties.DOCUMENTATION_PROPERTY);
        if (documentationProperty != null) {
            return getFile(pluginId, Paths.get(DOCS_DIRECTORY, documentationProperty), false);
        }

        return getFirstFile(pluginId, DOCS_DIRECTORY, includeNotDeployed);
    }

    @Override
    public String getDocumentation(String pluginId, String propertyKey, boolean fallbackToDocumentation, boolean includeNotDeployed)
        throws IOException {
        final T plugin = get(pluginId, includeNotDeployed);

        String documentationProperty = plugin.manifest().properties().get(propertyKey);
        if (documentationProperty != null) {
            return getFile(pluginId, Paths.get(DOCS_DIRECTORY, documentationProperty), includeNotDeployed);
        }

        if (fallbackToDocumentation) {
            return getDocumentation(pluginId, includeNotDeployed);
        }
        return null;
    }

    @Override
    public String getCategory(String pluginId) throws IOException {
        return getCategory(pluginId, false);
    }

    @Override
    public String getCategory(String pluginId, boolean includeNotDeployed) throws IOException {
        T plugin = get(pluginId, includeNotDeployed);
        if (plugin != null) {
            Map<String, String> properties = plugin.manifest().properties();
            if (properties != null) {
                return properties.get(PluginManifestProperties.MANIFEST_CATEGORY_PROPERTY);
            }
        }
        return null;
    }

    @Override
    public PluginMoreInformation getMoreInformation(String pluginId) throws IOException {
        return getMoreInformation(pluginId, false);
    }

    @Override
    public PluginMoreInformation getMoreInformation(String pluginId, boolean includeNotDeployed) throws IOException {
        T plugin = get(pluginId, includeNotDeployed);
        if (plugin != null) {
            Map<String, String> properties = plugin.manifest().properties();
            PluginMoreInformation pluginMoreInformation = new PluginMoreInformation();
            if (properties != null) {
                pluginMoreInformation.setDescription(properties.get(PluginManifestProperties.MORE_INFO_DESCRIPTION_PROPERTY));
                pluginMoreInformation.setDocumentationUrl(properties.get(PluginManifestProperties.MORE_INFO_DOCUMENTATION_URL_PROPERTY));
                pluginMoreInformation.setSchemaImg(
                    getFileFromPropertyAsBase64(plugin, properties, PluginManifestProperties.MORE_INFO_SCHEMA_IMG_PROPERTY)
                );
            }
            return pluginMoreInformation;
        }
        return null;
    }

    private String getFirstFile(String pluginId, String directory, boolean includeNotDeployed) throws IOException {
        final T plugin = get(pluginId, includeNotDeployed);

        if (plugin != null) {
            Path workspaceDir = plugin.path();

            final File dir = new File(workspaceDir.toString(), directory);
            if (!dir.exists()) {
                return null;
            }
            final File[] files = dir.listFiles(File::isFile);

            if (files != null && files.length > 0) {
                return new String(Files.readAllBytes(files[0].toPath()));
            }
            return null;
        }
        return null;
    }

    private String getFileFromPropertyAsBase64(T plugin, Map<String, String> properties, String property) throws IOException {
        if (properties != null && properties.containsKey(property)) {
            try {
                Path file = Paths.get(plugin.path().toString(), properties.get(property));
                String mimeType = Files.probeContentType(file);
                return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(Files.readAllBytes(file));
            } catch (NoSuchFileException ex) {
                logger.warn("File not found {}", plugin.path().toString());
            }
        }
        return null;
    }

    private String getFile(String pluginId, Path filePath, boolean includeNotDeployed) throws IOException {
        final T plugin = get(pluginId, includeNotDeployed);

        if (plugin != null) {
            Path workspaceDir = plugin.path();

            final File file = new File(workspaceDir.toString(), filePath.toString());

            if (file.exists() && file.isFile()) {
                return new String(Files.readAllBytes(file.toPath()));
            }
            return null;
        }
        return null;
    }
}
