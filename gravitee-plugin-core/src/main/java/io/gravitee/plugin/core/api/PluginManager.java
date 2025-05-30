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

import java.io.IOException;
import java.util.Collection;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface PluginManager<T extends Plugin> {
    void register(T plugin);

    Collection<T> findAll();

    Collection<T> findAll(boolean includeNotDeployed);

    T get(String pluginId);

    T get(String pluginId, boolean includeNotDeployed);

    String getIcon(String pluginId) throws IOException;

    String getIcon(String pluginId, boolean includeNotDeployed) throws IOException;

    /**
     * Get the documentation with the property key `PluginManifestProperties.DOCUMENTATION_PROPERTY`. If the property does not exist, the first file found in the `docs` folder will be returned.
     * @param pluginId the plugin id
     * @return the documentation as a {@link String}
     * @throws IOException if zip file errors occurred
     */
    String getDocumentation(String pluginId) throws IOException;
    /**
     * Get the documentation and language with the property key `PluginManifestProperties.DOCUMENTATION_PROPERTY`. If the property does not exist, the first file found in the `docs` folder will be returned.
     * @param pluginId the plugin id
     * @return the documentation as a {@link String}
     * @throws IOException if zip file errors occurred
     */
    PluginDocumentation getPluginDocumentation(String pluginId) throws IOException;

    /**
     * Get the documentation with the property key `PluginManifestProperties.DOCUMENTATION_PROPERTY`. If the property does not exist, the first file found in the `docs` folder will be returned.
     * @param pluginId the plugin id
     * @param includeNotDeployed whether to check for not deployed plugins
     * @return the documentation as a {@link String}
     * @throws IOException if zip file errors occurred
     */
    String getDocumentation(String pluginId, boolean includeNotDeployed) throws IOException;
    /**
     * Get the documentation and language with the property key `PluginManifestProperties.DOCUMENTATION_PROPERTY`. If the property does not exist, the first file found in the `docs` folder will be returned.
     * @param pluginId the plugin id
     * @return the documentation as a {@link String}
     * @throws IOException if zip file errors occurred
     */
    PluginDocumentation getPluginDocumentation(String pluginId, boolean includeNotDeployed) throws IOException;

    /**
     * Get documentation with a specific propertyKey.
     * @param pluginId the plugin id
     * @param propertyKey the property key to look for the documentation. e.g. `native_kafka.documentation`
     * @param fallbackToDocumentation if true and the propertyKey does not exist, the default {@link #getDocumentation(String, boolean)} will be called.
     * @param includeNotDeployed whether to check for not deployed plugins
     * @return the documentation as a {@link String}
     * @throws IOException if zip file errors occurred
     */
    String getDocumentation(String pluginId, String propertyKey, boolean fallbackToDocumentation, boolean includeNotDeployed)
        throws IOException;

    /**
     * Get documentation with a specific propertyKey as well as the markup language used based on file extension
     * @param pluginId the plugin id
     * @param propertyKey the property key to look for the documentation. e.g. `native_kafka.documentation`
     * @param fallbackToDocumentation if true and the propertyKey does not exist, the default {@link #getDocumentation(String, boolean)} will be called.
     * @param includeNotDeployed whether to check for not deployed plugins
     * @return the documentation as a {@link String}
     * @throws IOException if zip file errors occurred
     */
    PluginDocumentation getPluginDocumentation(
        String pluginId,
        String propertyKey,
        boolean fallbackToDocumentation,
        boolean includeNotDeployed
    ) throws IOException;

    String getCategory(String pluginId) throws IOException;

    String getCategory(String pluginId, boolean includeNotDeployed) throws IOException;

    PluginMoreInformation getMoreInformation(String pluginId) throws IOException;

    PluginMoreInformation getMoreInformation(String pluginId, boolean includeNotDeployed) throws IOException;
}
