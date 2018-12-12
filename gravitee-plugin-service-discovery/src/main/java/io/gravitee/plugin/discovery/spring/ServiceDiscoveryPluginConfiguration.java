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
package io.gravitee.plugin.discovery.spring;

import io.gravitee.plugin.core.api.ConfigurablePluginManager;
import io.gravitee.plugin.discovery.ServiceDiscoveryClassLoaderFactory;
import io.gravitee.plugin.discovery.ServiceDiscoveryPlugin;
import io.gravitee.plugin.discovery.internal.ServiceDiscoveryClassLoaderFactoryImpl;
import io.gravitee.plugin.discovery.internal.ServiceDiscoveryPluginManagerImpl;
import org.springframework.context.annotation.Bean;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class ServiceDiscoveryPluginConfiguration {

    @Bean
    public ConfigurablePluginManager<ServiceDiscoveryPlugin> serviceDiscoveryPluginManager() {
        return new ServiceDiscoveryPluginManagerImpl();
    }

    @Bean
    public ServiceDiscoveryClassLoaderFactory serviceDiscoveryClassLoaderFactory() {
        return new ServiceDiscoveryClassLoaderFactoryImpl();
    }
}
