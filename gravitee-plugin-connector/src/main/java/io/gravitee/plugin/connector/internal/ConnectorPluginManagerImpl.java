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
package io.gravitee.plugin.connector.internal;

import io.gravitee.connector.api.ConnectorFactory;
import io.gravitee.plugin.connector.ConnectorClassLoaderFactory;
import io.gravitee.plugin.connector.ConnectorPlugin;
import io.gravitee.plugin.connector.ConnectorPluginManager;
import io.gravitee.plugin.core.api.AbstractConfigurablePluginManager;
import io.gravitee.plugin.core.api.PluginClassLoader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class ConnectorPluginManagerImpl extends AbstractConfigurablePluginManager<ConnectorPlugin> implements ConnectorPluginManager {

    private final Logger logger = LoggerFactory.getLogger(ConnectorPluginManagerImpl.class);

    private final ConnectorClassLoaderFactory classLoaderFactory;

    private final Map<String, ConnectorFactory<?>> factories = new HashMap<>();
    private final Map<String, ConnectorFactory<?>> undeployedFactories = new HashMap<>();
    private final Map<String, ConnectorFactory<?>> factoriesByType = new HashMap<>();
    private final Map<String, ConnectorFactory<?>> undeployedFactoriesByType = new HashMap<>();

    public ConnectorPluginManagerImpl(final ConnectorClassLoaderFactory classLoaderFactory) {
        this.classLoaderFactory = classLoaderFactory;
    }

    @Override
    public void register(ConnectorPlugin plugin) {
        super.register(plugin);

        // Create connector
        PluginClassLoader pluginClassLoader = classLoaderFactory.getOrCreateClassLoader(plugin);
        try {
            final Class<ConnectorFactory> connectorFactoryClass = (Class<ConnectorFactory>) pluginClassLoader.loadClass(plugin.clazz());
            final ConnectorFactory factory = connectorFactoryClass.getDeclaredConstructor().newInstance();
            if (plugin.deployed()) {
                factories.put(plugin.id(), factory);
            } else {
                undeployedFactories.put(plugin.id(), factory);
            }

            final Collection<String> types = factory.supportedTypes();
            if (types != null) {
                for (String type : types) {
                    if (plugin.deployed()) {
                        factoriesByType.put(type.toLowerCase(), factory);
                    } else {
                        undeployedFactoriesByType.put(type.toLowerCase(), factory);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Unexpected error while loading connector plugin: {}", plugin.clazz(), ex);
        }
    }

    @Override
    public ConnectorFactory<?> getConnectorByType(String type) {
        return getConnectorByType(type, false);
    }

    @Override
    public ConnectorFactory<?> getConnectorByType(String type, boolean includeNotDeployed) {
        ConnectorFactory<?> factory = factoriesByType.get(type.toLowerCase());
        if (factory == null && includeNotDeployed) {
            return undeployedFactoriesByType.get(type.toLowerCase());
        }
        return factory;
    }

    @Override
    public ConnectorFactory<?> getConnector(String pluginId) {
        return factories.get(pluginId);
    }

    @Override
    public ConnectorFactory<?> getConnector(String pluginId, boolean includeNotDeployed) {
        ConnectorFactory<?> factory = factories.get(pluginId);
        if (factory == null && includeNotDeployed) {
            return undeployedFactories.get(pluginId);
        }
        return factory;
    }
}
