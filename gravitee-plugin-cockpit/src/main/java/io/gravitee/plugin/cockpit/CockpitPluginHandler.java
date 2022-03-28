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
package io.gravitee.plugin.cockpit;

import io.gravitee.cockpit.api.CockpitConnector;
import io.gravitee.plugin.core.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class CockpitPluginHandler extends AbstractSpringPluginHandler<CockpitConnector> {

    @Autowired
    private PluginClassLoaderFactory<Plugin> pluginClassLoaderFactory;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public boolean canHandle(Plugin plugin) {
        return plugin.type().equalsIgnoreCase(type());
    }

    @Override
    protected String type() {
        return PluginType.COCKPIT.name();
    }

    @Override
    protected void register(CockpitConnector connector) {
        try {
            // Start the cockpit connector.
            connector.start();
            registerBean(connector);
        } catch (Exception e) {
            logger.error("Unexpected error while starting cockpit controller", e);
        }
    }

    @Override
    protected ClassLoader getClassLoader(Plugin plugin) throws Exception {
        return pluginClassLoaderFactory.getOrCreateClassLoader(plugin, this.getClass().getClassLoader());
    }

    private void registerBean(CockpitConnector connector) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) (
            (ConfigurableApplicationContext) applicationContext
        ).getBeanFactory();

        beanFactory.registerSingleton(CockpitConnector.class.getName(), connector);
    }
}
