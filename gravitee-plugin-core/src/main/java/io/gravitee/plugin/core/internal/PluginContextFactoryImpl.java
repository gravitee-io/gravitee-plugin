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
package io.gravitee.plugin.core.internal;

import io.gravitee.plugin.core.api.Plugin;
import io.gravitee.plugin.core.api.PluginContextConfigurer;
import io.gravitee.plugin.core.api.PluginContextFactory;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author David BRASSELY (david at gravitee.io)
 * @author GraviteeSource Team
 */
public class PluginContextFactoryImpl implements PluginContextFactory, ApplicationContextAware {

    protected final Logger LOGGER = LoggerFactory.getLogger(PluginContextFactoryImpl.class);

    private final Map<Plugin, ConfigurableApplicationContext> pluginContexts = new HashMap<>();

    private ApplicationContext containerContext;

    @Override
    public ApplicationContext create(PluginContextConfigurer pluginContextConfigurer) {
        // Autowire configurer bean
        containerContext.getAutowireCapableBeanFactory().autowireBean(pluginContextConfigurer);

        Plugin plugin = pluginContextConfigurer.plugin();
        LOGGER.debug("Create context for plugin: {}", plugin.id());

        ConfigurableApplicationContext pluginContext = pluginContextConfigurer.applicationContext();
        pluginContextConfigurer.registerBeanFactoryPostProcessor();
        pluginContextConfigurer.registerBeans();

        ClassLoader pluginClassLoader = pluginContextConfigurer.classLoader();
        ClassLoader containerClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(pluginClassLoader);
            pluginContext.refresh();
        } catch (Exception ex) {
            LOGGER.error("Unable to refresh plugin context", ex);
        } finally {
            Thread.currentThread().setContextClassLoader(containerClassLoader);
        }

        pluginContexts.putIfAbsent(plugin, pluginContext);

        return pluginContext;
    }

    @Override
    public ApplicationContext create(Plugin plugin) {
        return create(new AnnotationBasedPluginContextConfigurer(plugin));
    }

    @Override
    public void remove(Plugin plugin) {
        ConfigurableApplicationContext ctx = pluginContexts.remove(plugin);
        if (ctx != null) {
            ctx.close();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.containerContext = applicationContext;
    }
}
