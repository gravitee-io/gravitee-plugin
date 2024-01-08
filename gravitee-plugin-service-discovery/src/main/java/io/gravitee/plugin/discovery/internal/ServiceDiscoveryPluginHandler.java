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
package io.gravitee.plugin.discovery.internal;

import io.gravitee.plugin.core.api.AbstractSimplePluginHandler;
import io.gravitee.plugin.core.api.ConfigurablePluginManager;
import io.gravitee.plugin.core.api.Plugin;
import io.gravitee.plugin.discovery.ServiceDiscoveryPlugin;
import io.gravitee.plugin.discovery.spring.ServiceDiscoveryPluginConfiguration;
import java.net.URLClassLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@Import(ServiceDiscoveryPluginConfiguration.class)
public class ServiceDiscoveryPluginHandler extends AbstractSimplePluginHandler<ServiceDiscoveryPlugin> {

    @Autowired
    private ConfigurablePluginManager<ServiceDiscoveryPlugin> serviceDiscoveryPluginManager;

    @Override
    public boolean canHandle(Plugin plugin) {
        return ServiceDiscoveryPlugin.PLUGIN_TYPE.equalsIgnoreCase(plugin.type());
    }

    @Override
    protected String type() {
        return "service-discoveries";
    }

    @Override
    protected ServiceDiscoveryPlugin create(Plugin plugin, Class<?> pluginClass) {
        ServiceDiscoveryPluginImpl serviceDiscoveryPlugin = new ServiceDiscoveryPluginImpl(plugin, pluginClass);
        serviceDiscoveryPlugin.setConfiguration(new ServiceDiscoveryConfigurationClassFinder().lookupFirst(pluginClass));

        return serviceDiscoveryPlugin;
    }

    @Override
    protected void register(ServiceDiscoveryPlugin plugin) {
        serviceDiscoveryPluginManager.register(plugin);
    }

    @Override
    protected ClassLoader getClassLoader(Plugin plugin) throws Exception {
        return new URLClassLoader(plugin.dependencies(), this.getClass().getClassLoader());
    }
}
