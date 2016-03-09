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
import io.gravitee.plugin.core.api.PluginConfigurationResolver;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * @author David BRASSELY (brasseld at gmail.com)
 * @author GraviteeSource Team
 */
public class ReflectionBasedPluginConfigurationResolver implements PluginConfigurationResolver {

    private final Logger LOGGER = LoggerFactory.getLogger(ReflectionBasedPluginConfigurationResolver.class);

    @Override
    public Set<Class<?>> resolve(Plugin plugin) {
        LOGGER.debug("Looking for configurations for plugin {} in package {}", plugin.clazz().getName(),
                plugin.clazz().getPackage().getName());

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .addClassLoader(plugin.clazz().getClassLoader())
                .setUrls(ClasspathHelper.forClass(plugin.clazz(), plugin.clazz().getClassLoader()))
                .setScanners(new SubTypesScanner(false), new TypeAnnotationsScanner())
                .filterInputsBy(new FilterBuilder().includePackage(plugin.clazz().getPackage().getName())));

        return reflections.getTypesAnnotatedWith(Configuration.class);
    }
}
