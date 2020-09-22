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
package io.gravitee.plugin.alert.internal;

import io.gravitee.alert.api.event.EventProducer;
import io.gravitee.alert.api.trigger.TriggerProvider;
import io.gravitee.plugin.alert.AlertEventProducerManager;
import io.gravitee.plugin.alert.AlertPlugin;
import io.gravitee.plugin.alert.AlertTriggerProviderManager;
import io.gravitee.plugin.core.api.AbstractSpringPluginHandler;
import io.gravitee.plugin.core.api.Plugin;
import io.gravitee.plugin.core.api.PluginClassLoaderFactory;
import io.gravitee.plugin.core.api.PluginContextFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * @author Azize ELAMRANI (azize.elamrani at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AlertPluginHandler extends AbstractSpringPluginHandler<Void> {

    @Autowired
    private PluginContextFactory pluginContextFactory;

    @Autowired
    private PluginClassLoaderFactory pluginClassLoaderFactory;

    @Autowired
    private AlertEventProducerManager eventProducerManager;

    @Autowired
    private AlertTriggerProviderManager triggerProviderManager;

    @Override
    public boolean canHandle(Plugin plugin) {
        return AlertPlugin.PLUGIN_TYPE.equalsIgnoreCase(plugin.type());
    }

    @Override
    protected void handle(Plugin plugin, Class pluginClass) {
        try {
            ApplicationContext context = pluginContextFactory.create(plugin);

            // Look for an event producer
            try {
                eventProducerManager.register(context.getBean(EventProducer.class));
            } catch (NoSuchBeanDefinitionException nsbee) {
                // No event producer to register
            }

            // Look for a trigger provider
            try {
                triggerProviderManager.register(context.getBean(TriggerProvider.class));
            } catch (NoSuchBeanDefinitionException nsbee) {
                // No event producer to register
            }
        } catch (Exception ex) {
            logger.error("Unexpected error while creating {}", plugin.id(), ex);

            // Be sure that the context does not exist anymore.
            pluginContextFactory.remove(plugin);
        }
    }

    @Override
    protected String type() {
        return "alerts";
    }

    @Override
    protected ClassLoader getClassLoader(Plugin plugin) throws Exception {
        return pluginClassLoaderFactory.getOrCreateClassLoader(plugin, this.getClass().getClassLoader());
    }

    @Override
    protected void register(Void plugin) {
        // Nothing to do there
    }
}
