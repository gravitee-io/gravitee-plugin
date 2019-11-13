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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.ParameterizedType;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public abstract class AbstractSpringPluginHandler<T> extends AbstractPluginHandler {

    @Autowired
    private PluginContextFactory pluginContextFactory;

    private final Class<T> pluginClass;

    public AbstractSpringPluginHandler() {
        this.pluginClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
    }

    @Override
    protected void handle(Plugin plugin, Class<?> pluginClass) {
        try {
            ApplicationContext context = pluginContextFactory.create(plugin);
            T pluginInst = context.getBean(this.pluginClass);
            register(pluginInst);
        } catch (Exception ex) {
            logger.error("Unexpected error while creating {}", plugin.id(), ex);

            // Be sure that the context does not exist anymore.
            pluginContextFactory.remove(plugin);
        }
    }

    protected abstract void register(T plugin);
}
