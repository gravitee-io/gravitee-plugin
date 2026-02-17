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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * Special Spring {@link BeanDefinitionRegistryPostProcessor} that can be implemented to auto-detect and register {@link PluginHandler}
 * beans into the Spring registry.
 * It differs from {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor} in a sense that it allows for registering beans
 * before the Spring context has been refreshed and allows for support of {@link org.springframework.context.annotation.Import} annotation.
 *
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
@Slf4j
abstract class AbstractPluginHandlerBeanRegistryPostProcessor<T extends PluginHandler>
    implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {

    private static final Pattern TYPE_NAME_PATTERN = Pattern.compile("(?<=[a-z])[A-Z]");
    protected final Class<T> type;
    protected final String typeName;

    public AbstractPluginHandlerBeanRegistryPostProcessor(Class<T> type) {
        this.type = type;

        final Matcher m = TYPE_NAME_PATTERN.matcher(type.getSimpleName());
        this.typeName = m.replaceAll(match -> " " + match.group()).toLowerCase();
    }

    @Override
    public void postProcessBeanDefinitionRegistry(@Nonnull BeanDefinitionRegistry registry) throws BeansException {
        log.info("Loading {}s", typeName);

        final AnnotatedBeanDefinitionReader annotatedBeanDefinitionReader = new AnnotatedBeanDefinitionReader(registry);
        final List<? extends Class<?>> pluginHandlers = SpringFactoriesLoader.loadFactories(type, this.getClass().getClassLoader())
            .stream()
            .map(t -> t.getClass())
            .toList();

        log.info("Found {} {}(s):", pluginHandlers.size(), typeName);

        for (Class<?> pluginHandlerClass : pluginHandlers) {
            annotatedBeanDefinitionReader.registerBean(pluginHandlerClass, pluginHandlerClass.getName());
            log.info("\t{}", pluginHandlerClass.getName());
        }
    }

    @Override
    public void postProcessBeanFactory(@Nonnull ConfigurableListableBeanFactory beanFactory) throws BeansException {}

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
