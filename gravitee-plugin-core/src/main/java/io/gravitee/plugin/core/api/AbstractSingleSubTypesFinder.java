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

import java.util.Collection;

/**
 * @author David BRASSELY (david at gravitee.io)
 * @author GraviteeSource Team
 */
public abstract class AbstractSingleSubTypesFinder<T> extends AbstractSubTypesFinder<T> {

    protected AbstractSingleSubTypesFinder(Class<T> subType) {
        super(subType);
    }

    public Class<? extends T> lookupFirst(Class clazz, ClassLoader classLoader) {
        Collection<Class<? extends T>> classes = lookup(clazz, classLoader);
        return classes.isEmpty() ? null : classes.iterator().next();
    }
}
