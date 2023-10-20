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
package io.gravitee.plugin.core.spring;

import io.gravitee.plugin.core.api.PluginHandler;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * @author David BRASSELY (brasseld at gmail.com)
 * @author GraviteeSource Team
 */
public class PluginHandlerBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    private final Logger LOGGER = LoggerFactory.getLogger(PluginHandlerBeanFactoryPostProcessor.class);

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        LOGGER.info("Loading plugin handlers");

        Set<String> pluginHandlers = new HashSet<>(
            SpringFactoriesLoader.loadFactoryNames(PluginHandler.class, beanFactory.getBeanClassLoader())
        );

        LOGGER.info("Find {} plugin handler(s):", pluginHandlers.size());

        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) beanFactory;

        for (String pluginHandlerClass : pluginHandlers) {
            try {
                Class<?> instanceClass = ClassUtils.forName(pluginHandlerClass, beanFactory.getBeanClassLoader());
                Assert.isAssignable(PluginHandler.class, instanceClass);

                PluginHandler pluginHandler = createInstance((Class<PluginHandler>) instanceClass);
                defaultListableBeanFactory.registerBeanDefinition(
                    pluginHandler.getClass().getName(),
                    new RootBeanDefinition(pluginHandler.getClass().getName())
                );

                LOGGER.info("\t{}", pluginHandler.getClass().getName());
            } catch (Exception ex) {
                LOGGER.error("Unable to instantiate plugin handler: {}", ex);
                throw new IllegalStateException("Unable to instantiate plugin handler: " + pluginHandlerClass, ex);
            }
        }
    }

    private <T> T createInstance(Class<T> clazz) throws Exception {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            LOGGER.error("Unable to instantiate class: {}", ex);
            throw ex;
        }
    }
}
