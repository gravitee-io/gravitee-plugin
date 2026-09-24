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
package io.gravitee.plugin.agentprovider.internal;

import io.gravitee.agentprovider.api.AgentProviderFactory;
import io.gravitee.plugin.agentprovider.AgentProviderPlugin;
import io.gravitee.plugin.agentprovider.AgentProviderPluginManager;
import io.gravitee.plugin.core.api.AbstractConfigurablePluginManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author GraviteeSource Team
 */
public class DefaultAgentProviderPluginManager
    extends AbstractConfigurablePluginManager<AgentProviderPlugin>
    implements AgentProviderPluginManager {

    private final Map<String, AgentProviderFactory> factories = new ConcurrentHashMap<>();

    public void register(AgentProviderPlugin plugin, AgentProviderFactory factory) {
        super.register(plugin);
        factories.put(plugin.id(), factory);
    }

    @Override
    public AgentProviderFactory getAgentProviderFactory(String pluginId) {
        return factories.get(pluginId);
    }
}
