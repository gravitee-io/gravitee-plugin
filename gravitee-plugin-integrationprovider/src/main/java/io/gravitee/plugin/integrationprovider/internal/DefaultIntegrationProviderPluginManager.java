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
package io.gravitee.plugin.integrationprovider.internal;

import io.gravitee.integration.api.plugin.IntegrationProviderFactory;
import io.gravitee.plugin.core.api.AbstractConfigurablePluginManager;
import io.gravitee.plugin.core.api.PluginClassLoader;
import io.gravitee.plugin.integrationprovider.IntegrationProviderClassLoaderFactory;
import io.gravitee.plugin.integrationprovider.IntegrationProviderPlugin;
import io.gravitee.plugin.integrationprovider.IntegrationProviderPluginManager;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Remi Baptiste (remi.baptiste at graviteesource.com)
 * @author GraviteeSource Team
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultIntegrationProviderPluginManager
    extends AbstractConfigurablePluginManager<IntegrationProviderPlugin>
    implements IntegrationProviderPluginManager {

    private final IntegrationProviderClassLoaderFactory classLoaderFactory;

    private final Map<String, IntegrationProviderFactory<?>> factories = new HashMap<>();
    private final Map<String, IntegrationProviderFactory<?>> undeployedFactories = new HashMap<>();

    @Override
    public void register(IntegrationProviderPlugin plugin) {
        super.register(plugin);

        // Create integrationProvider
        PluginClassLoader pluginClassLoader = classLoaderFactory.getOrCreateClassLoader(plugin);
        try {
            final Class<IntegrationProviderFactory<?>> integrationProviderFactoryClass = (Class<
                IntegrationProviderFactory<?>
            >) pluginClassLoader.loadClass(plugin.clazz());
            final IntegrationProviderFactory<?> factory = integrationProviderFactoryClass.getDeclaredConstructor().newInstance();
            if (plugin.deployed()) {
                factories.put(plugin.id(), factory);
            } else {
                undeployedFactories.put(plugin.id(), factory);
            }
        } catch (Exception ex) {
            log.error("Unexpected error while loading integration provider plugin: {}", plugin.clazz(), ex);
        }
    }

    @Override
    public IntegrationProviderFactory<?> getIntegrationProviderFactory(String pluginId) {
        return factories.get(pluginId);
    }

    @Override
    public IntegrationProviderFactory<?> getIntegrationProviderFactory(String pluginId, boolean includeNotDeployed) {
        IntegrationProviderFactory<?> factory = factories.get(pluginId);
        if (factory == null && includeNotDeployed) {
            return undeployedFactories.get(pluginId);
        }
        return factory;
    }
}
