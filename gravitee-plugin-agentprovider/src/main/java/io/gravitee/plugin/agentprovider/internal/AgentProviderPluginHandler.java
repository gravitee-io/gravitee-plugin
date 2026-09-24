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
import io.gravitee.plugin.agentprovider.spring.AgentProviderPluginConfiguration;
import io.gravitee.plugin.core.api.AbstractSimplePluginHandler;
import io.gravitee.plugin.core.api.Plugin;
import io.gravitee.plugin.core.api.PluginClassLoaderFactory;
import io.gravitee.plugin.core.api.PluginContextFactory;
import io.gravitee.plugin.core.internal.AnnotationBasedPluginContextConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

/**
 * Each deployed agent provider gets its own Spring context, child of the host one, and its factory is a bean of that
 * context: it can inject host beans and the plugin can declare its own {@code @Configuration} classes.
 *
 * @author GraviteeSource Team
 */
@Import(AgentProviderPluginConfiguration.class)
public class AgentProviderPluginHandler extends AbstractSimplePluginHandler<AgentProviderPlugin> {

    @Autowired
    private PluginClassLoaderFactory<Plugin> pluginClassLoaderFactory;

    @Autowired
    private PluginContextFactory pluginContextFactory;

    @Autowired
    private DefaultAgentProviderPluginManager agentProviderPluginManager;

    @Override
    public boolean canHandle(Plugin plugin) {
        return AgentProviderPlugin.PLUGIN_TYPE.equalsIgnoreCase(plugin.type());
    }

    @Override
    protected String type() {
        return "agent-providers";
    }

    @Override
    protected AgentProviderPlugin create(Plugin plugin, Class<?> pluginClass) {
        DefaultAgentProviderPlugin agentProviderPlugin = new DefaultAgentProviderPlugin(plugin, pluginClass);
        agentProviderPlugin.setConfiguration(new AgentProviderConfigurationClassFinder().lookupFirst(pluginClass));

        return agentProviderPlugin;
    }

    @Override
    protected void register(AgentProviderPlugin agentProviderPlugin) {
        if (!agentProviderPlugin.deployed()) {
            agentProviderPluginManager.register(agentProviderPlugin);
            return;
        }

        try {
            ApplicationContext pluginContext = pluginContextFactory.create(new AnnotationBasedPluginContextConfigurer(agentProviderPlugin));
            AgentProviderFactory factory = (AgentProviderFactory) pluginContext.getBean(agentProviderPlugin.agentProvider());
            agentProviderPluginManager.register(agentProviderPlugin, factory);
        } catch (Exception e) {
            logger.error("Unexpected error while creating the factory of agent provider {}", agentProviderPlugin.id(), e);
            pluginContextFactory.remove(agentProviderPlugin);
            // Still listed, so the console can show its schema and documentation.
            agentProviderPluginManager.register(agentProviderPlugin);
        }
    }

    @Override
    protected ClassLoader getClassLoader(Plugin plugin) {
        return pluginClassLoaderFactory.getOrCreateClassLoader(plugin, this.getClass().getClassLoader());
    }
}
