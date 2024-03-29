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
package io.gravitee.plugin.fetcher.spring;

import io.gravitee.plugin.core.api.ConfigurablePluginManager;
import io.gravitee.plugin.fetcher.FetcherClassLoaderFactory;
import io.gravitee.plugin.fetcher.FetcherPlugin;
import io.gravitee.plugin.fetcher.internal.FetcherClassLoaderFactoryImpl;
import io.gravitee.plugin.fetcher.internal.FetcherPluginManagerImpl;
import org.springframework.context.annotation.Bean;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class FetcherPluginConfiguration {

    @Bean
    public ConfigurablePluginManager<FetcherPlugin> fetcherPluginManager() {
        return new FetcherPluginManagerImpl();
    }

    @Bean
    public FetcherClassLoaderFactory fetcherClassLoaderFactory() {
        return new FetcherClassLoaderFactoryImpl();
    }
}
