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
package io.gravitee.plugin.core.spring;

import io.gravitee.plugin.core.api.PluginClassLoaderFactory;
import io.gravitee.plugin.core.api.PluginConfigurationResolver;
import io.gravitee.plugin.core.api.PluginContextFactory;
import io.gravitee.plugin.core.api.PluginRegistry;
import io.gravitee.plugin.core.internal.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@Configuration
public class PluginConfiguration {

    public static final int PARALLELISM = Runtime.getRuntime().availableProcessors() * 2;

    @Bean
    public PluginRegistryConfiguration pluginRegistryConfiguration() {
        return new PluginRegistryConfiguration();
    }

    @Bean
    public PluginRegistry pluginRegistry() {
        return new PluginRegistryImpl();
    }

    @Bean(name = "pluginClassLoaderFactory")
    public PluginClassLoaderFactory classLoaderFactory() {
        return new CachedPluginClassLoaderFactory();
    }

    @Bean
    public PluginContextFactory pluginContextFactory() {
        return new PluginContextFactoryImpl();
    }

    @Bean
    public PluginEventListener pluginEventListener() {
        return new PluginEventListener();
    }

    @Bean
    public PluginConfigurationResolver pluginConfigurationResolver() {
        return new ReflectionBasedPluginConfigurationResolver();
    }

    @Bean
    public static PluginHandlerBeanFactoryPostProcessor pluginHandlerBeanFactoryPostProcessor() {
        return new PluginHandlerBeanFactoryPostProcessor();
    }

    @Bean("corePluginExecutor")
    public ThreadPoolExecutor syncExecutor() {
        final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(PARALLELISM, PARALLELISM, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadFactory() {
                    private int counter = 0;

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "gio.core-plugin-" + counter++);
                    }
                });

        threadPoolExecutor.allowCoreThreadTimeOut(true);

        return threadPoolExecutor;
    }
}
