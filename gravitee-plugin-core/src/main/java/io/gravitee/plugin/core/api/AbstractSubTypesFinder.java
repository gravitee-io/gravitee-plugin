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
package io.gravitee.plugin.core.api;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.util.Collection;

/**
 * @author David BRASSELY (david at gravitee.io)
 * @author GraviteeSource Team
 */
public abstract class AbstractSubTypesFinder<T> implements SubTypesFinder<T> {

    private final Class<T> subType;

    protected AbstractSubTypesFinder(Class<T> subType) {
        this.subType = subType;
    }

    @Override
    public Collection<Class<? extends T>> lookup(Class<?> clazz, ClassLoader classLoader) {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .addClassLoader(classLoader)
                .setExpandSuperTypes(false)
                .setUrls(ClasspathHelper.forClass(clazz, classLoader))
                .setScanners(new SubTypesScanner(true), new TypeAnnotationsScanner())
                .filterInputsBy(new FilterBuilder().includePackage(clazz.getPackage().getName())));

        return reflections.getSubTypesOf(subType);
    }
}
