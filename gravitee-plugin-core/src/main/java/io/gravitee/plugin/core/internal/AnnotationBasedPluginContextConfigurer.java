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
package io.gravitee.plugin.core.internal;

import io.gravitee.plugin.core.api.Plugin;
import io.gravitee.plugin.core.api.PluginClassLoaderFactory;
import io.gravitee.plugin.core.api.PluginConfigurationResolver;
import io.gravitee.plugin.core.api.PluginContextConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AnnotationBasedPluginContextConfigurer implements PluginContextConfigurer {

    private final Logger LOGGER = LoggerFactory.getLogger(AnnotationBasedPluginContextConfigurer.class);

    @Autowired
    private PluginConfigurationResolver pluginConfigurationResolver;

    @Autowired
    private PluginClassLoaderFactory pluginClassLoaderFactory;

    @Autowired
    private ApplicationContext containerContext;

    private GenericApplicationContext pluginContext;

    private final Plugin plugin;

    public AnnotationBasedPluginContextConfigurer(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Plugin plugin() {
        return plugin;
    }

    @Override
    public ClassLoader classLoader() {
        return pluginClassLoaderFactory.getOrCreateClassLoader(plugin);
    }

    @Override
    public ConfigurableEnvironment environment() {
        return (ConfigurableEnvironment) containerContext.getEnvironment();
    }

    @Override
    public ConfigurableApplicationContext applicationContext() {
        if (pluginContext == null) {
            LOGGER.debug("Initializing a new plugin context for {}", plugin.id());
            AnnotationConfigApplicationContext configApplicationContext = new AnnotationConfigApplicationContext();
            configApplicationContext.setClassLoader(classLoader());
            configApplicationContext.setEnvironment(environment());
            configApplicationContext.setParent(containerContext);

            Set<Class<?>> configurations = configurations();
            LOGGER.debug("Registering following @Configuration classes for {}: {}", plugin.id(),
                    configurations.stream().map(Class::getName).collect(Collectors.joining(", ")));

            configurations.forEach(configApplicationContext::register);
            pluginContext = configApplicationContext;
        }

        return pluginContext;
    }

    @Override
    public Set<Class<?>> configurations() {
        return pluginConfigurationResolver.resolve(plugin);
    }

    @Override
    public void registerBeans() {
        // This specific case should be handle by the plugin handler while creating the plugin context
        // TODO: find a way to handle this properly
        if (! plugin.type().equalsIgnoreCase("policy")) {
            pluginContext.registerBeanDefinition(plugin.clazz(),
                    BeanDefinitionBuilder.rootBeanDefinition(plugin.clazz()).getBeanDefinition());
        }
    }

    @Override
    public void registerBeanFactoryPostProcessor() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setIgnoreUnresolvablePlaceholders(true);
        configurer.setEnvironment(pluginContext.getEnvironment());
        pluginContext.addBeanFactoryPostProcessor(configurer);
    }
}
