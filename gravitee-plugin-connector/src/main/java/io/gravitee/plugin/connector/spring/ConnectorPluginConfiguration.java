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
package io.gravitee.plugin.connector.spring;

import io.gravitee.plugin.connector.ConnectorClassLoaderFactory;
import io.gravitee.plugin.connector.ConnectorPlugin;
import io.gravitee.plugin.connector.internal.ConnectorClassLoaderFactoryImpl;
import io.gravitee.plugin.connector.internal.ConnectorPluginManagerImpl;
import io.gravitee.plugin.core.api.ConfigurablePluginManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@Configuration
public class ConnectorPluginConfiguration {

    @Bean
    public ConfigurablePluginManager<ConnectorPlugin> connectorPluginManager(final ConnectorClassLoaderFactory connectorClassLoaderFactory) {
        return new ConnectorPluginManagerImpl(connectorClassLoaderFactory);
    }

    @Bean
    public ConnectorClassLoaderFactory connectorClassLoaderFactory() {
        return new ConnectorClassLoaderFactoryImpl();
    }
}
