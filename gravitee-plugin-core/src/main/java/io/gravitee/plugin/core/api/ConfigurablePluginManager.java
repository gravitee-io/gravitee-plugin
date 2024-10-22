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

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface ConfigurablePluginManager<T extends ConfigurablePlugin> extends PluginManager<T> {
    /**
     * Get the schema with the property key `PluginManifestProperties.SCHEMA_PROPERTY`. If the property does not exist, the first file found in the `schemas` folder will be returned.
     * @param pluginId the plugin id
     * @return the schema as a {@link String}
     * @throws IOException
     */
    String getSchema(String pluginId) throws IOException;

    /**
     * Get the schema with the property key `PluginManifestProperties.SCHEMA_PROPERTY`. If the property does not exist, the first file found in the `schemas` folder will be returned.
     * @param pluginId the plugin id
     * @param includeNotDeployed whether to check for not deployed plugins
     * @return the schema as a {@link String}
     * @throws IOException
     */
    String getSchema(String pluginId, boolean includeNotDeployed) throws IOException;

    /**
     * Get schema in a subfolder.
     * @param pluginId is the id of the plugin we want to retrieve schema
     * @param subFolder is the sub folder on which looks for the schema. Example: <code>getSchema("webhook", "subscriptions")</code>
     * @return the schema as a {@link String}
     * @throws IOException
     * @deprecated use {@link #getSchema(String pluginId, String property, boolean fallbackToSchema, boolean includeNotDeployed)} instead.
     * We prefer not to link the folder structure but a property key with the main product. This allows to have a plugin.properties file containing a visible configuration for the plugin.
     */
    @Deprecated
    String getSchema(String pluginId, String subFolder) throws IOException;

    /**
     * @deprecated use {@link #getSchema(String pluginId, String property, boolean fallbackToSchema, boolean includeNotDeployed)} instead.
     * We prefer not to link the folder structure but a property key with the main product. This allows to have a plugin.properties file containing a visible configuration for the plugin.
     */
    @Deprecated
    String getSchema(String pluginId, String subFolder, boolean includeNotDeployed) throws IOException;

    /**
     * Get the schema with the specified propertyKey.
     * @param pluginId the plugin id
     * @param propertyKey is the property key on which looks for the schema. ex: `native_kafka.schemas` or `schema.subscriptions`
     * @param fallbackToSchema if true and the propertyKey does not exist, the default {@link #getSchema(String, boolean)} will be called.
     * @param includeNotDeployed whether to check for not deployed plugins
     * @return
     * @throws IOException
     */
    String getSchema(String pluginId, String propertyKey, boolean fallbackToSchema, boolean includeNotDeployed) throws IOException;
}
