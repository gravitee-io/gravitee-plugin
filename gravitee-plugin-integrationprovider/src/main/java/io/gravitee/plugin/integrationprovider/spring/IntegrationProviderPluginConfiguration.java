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
package io.gravitee.plugin.integrationprovider.spring;

import io.gravitee.plugin.integrationprovider.IntegrationProviderClassLoaderFactory;
import io.gravitee.plugin.integrationprovider.internal.DefaultIntegrationProviderClassLoaderFactory;
import io.gravitee.plugin.integrationprovider.internal.DefaultIntegrationProviderPluginManager;
import io.gravitee.plugin.integrationprovider.internal.IntegrationProviderPluginHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Remi Baptiste (remi.baptiste at graviteesource.com)
 * @author GraviteeSource Team
 */
@Configuration
public class IntegrationProviderPluginConfiguration {

    @Bean
    public DefaultIntegrationProviderPluginManager integrationProviderPluginManager(
        final IntegrationProviderClassLoaderFactory integrationProviderClassLoaderFactory
    ) {
        return new DefaultIntegrationProviderPluginManager(integrationProviderClassLoaderFactory);
    }

    @Bean
    public IntegrationProviderClassLoaderFactory integrationProviderClassLoaderFactory() {
        return new DefaultIntegrationProviderClassLoaderFactory();
    }

    @Bean
    IntegrationProviderPluginHandler integrationProviderPluginHandler() {
        return new IntegrationProviderPluginHandler();
    }
}
