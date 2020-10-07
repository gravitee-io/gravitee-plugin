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
package io.gravitee.plugin.core.api;

import io.gravitee.plugin.core.internal.PluginManifestProperties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public abstract class AbstractConfigurablePluginManager<T extends ConfigurablePlugin>
    extends AbstractPluginManager<T> implements ConfigurablePluginManager<T> {

    private final static String SCHEMAS_DIRECTORY = "schemas";

    private final static String DOCS_DIRECTORY = "docs";

    @Override
    public String getSchema(String pluginId) throws IOException {
        return getFirstFile(pluginId, SCHEMAS_DIRECTORY);
    }

    @Override
    public String getIcon(String pluginId) throws IOException {
        T plugin = get(pluginId);
        Map<String, String> properties = plugin.manifest().properties();
        if (properties != null) {
            String icon = properties.get(PluginManifestProperties.MANIFEST_ICON_PROPERTY);
            if (icon != null) {
                Path iconFile = Paths.get(plugin.path().toString(), icon);
                String mimeType = Files.probeContentType(iconFile);
                return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(Files.readAllBytes(iconFile));
            }
        }

        return null;
    }

    @Override
    public String getDocumentation(String pluginId) throws IOException {
        return getFirstFile(pluginId, DOCS_DIRECTORY);
    }

    @Override
    public String getCategory(String pluginId) throws IOException {
        T plugin = get(pluginId);
        Map<String, String> properties = plugin.manifest().properties();
        if (properties != null) {
            return properties.get(PluginManifestProperties.MANIFEST_CATEGORY_PROPERTY);
        }
        return null;
    }


    private String getFirstFile(String pluginId, String directory) throws IOException {
        Path workspaceDir = get(pluginId).path();

        File[] matches = workspaceDir.toFile().listFiles(
            pathname -> pathname.isDirectory() && pathname.getName().equals(directory));

        if (matches.length == 1) {
            File dir = matches[0];

            if (dir.listFiles().length > 0) {
                return new String(Files.readAllBytes(dir.listFiles()[0].toPath()));
            }
        }

        return null;
    }
}
