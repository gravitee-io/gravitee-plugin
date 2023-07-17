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
    String getSchema(String pluginId) throws IOException;

    String getSchema(String pluginId, boolean includeDisabled) throws IOException;

    /**
     * Get schema in a subfolder.
     * @param pluginId is the id of the plugin we want to retrieve schema
     * @param subFolder is the sub folder on which looks for the schema. Example: <code>getSchema("webhook", "subscriptions")</code>
     * @return the schema as a {@link String}
     * @throws IOException
     */
    String getSchema(String pluginId, String subFolder) throws IOException;

    String getSchema(String pluginId, String subFolder, boolean includeDisabled) throws IOException;
}
