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
package io.gravitee.plugin.cloudserviceprovider.internal;

import io.gravitee.cloudservice.api.plugin.CloudServiceProviderFactory;
import io.gravitee.plugin.cloudserviceprovider.CloudServiceProviderClassLoaderFactory;
import io.gravitee.plugin.cloudserviceprovider.CloudServiceProviderPlugin;
import io.gravitee.plugin.cloudserviceprovider.CloudServiceProviderPluginManager;
import io.gravitee.plugin.core.api.AbstractConfigurablePluginManager;
import io.gravitee.plugin.core.api.PluginClassLoader;
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
public class DefaultCloudServiceProviderPluginManager
    extends AbstractConfigurablePluginManager<CloudServiceProviderPlugin>
    implements CloudServiceProviderPluginManager {

    private final CloudServiceProviderClassLoaderFactory classLoaderFactory;

    private final Map<String, CloudServiceProviderFactory<?>> factories = new HashMap<>();
    private final Map<String, CloudServiceProviderFactory<?>> undeployedFactories = new HashMap<>();

    @Override
    public void register(CloudServiceProviderPlugin plugin) {
        super.register(plugin);

        // Create cloudServiceProvider
        PluginClassLoader pluginClassLoader = classLoaderFactory.getOrCreateClassLoader(plugin);
        try {
            final Class<CloudServiceProviderFactory<?>> cloudServiceProviderFactoryClass =
                (Class<CloudServiceProviderFactory<?>>) pluginClassLoader.loadClass(plugin.clazz());
            final CloudServiceProviderFactory<?> factory = cloudServiceProviderFactoryClass.getDeclaredConstructor().newInstance();
            if (plugin.deployed()) {
                factories.put(plugin.id(), factory);
            } else {
                undeployedFactories.put(plugin.id(), factory);
            }
        } catch (Exception ex) {
            log.error("Unexpected error while loading cloud service provider plugin: {}", plugin.clazz(), ex);
        }
    }

    @Override
    public CloudServiceProviderFactory<?> getCloudServiceProviderFactory(String pluginId) {
        return factories.get(pluginId);
    }

    @Override
    public CloudServiceProviderFactory<?> getCloudServiceProviderFactory(String pluginId, boolean includeNotDeployed) {
        CloudServiceProviderFactory<?> factory = factories.get(pluginId);
        if (factory == null && includeNotDeployed) {
            return undeployedFactories.get(pluginId);
        }
        return factory;
    }
}
