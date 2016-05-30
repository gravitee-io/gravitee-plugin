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
package io.gravitee.plugin.resource.internal;

import io.gravitee.plugin.core.api.Plugin;
import io.gravitee.plugin.core.api.PluginHandler;
import io.gravitee.plugin.core.api.PluginType;
import io.gravitee.plugin.resource.ResourcePluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.net.URLClassLoader;

/**
 * @author David BRASSELY (david at gravitee.io)
 * @author GraviteeSource Team
 */
public class ResourcePluginHandler implements PluginHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(ResourcePluginHandler.class);

    @Autowired
    private ResourcePluginManager resourcePluginManager;

    @Override
    public boolean canHandle(Plugin plugin) {
        return PluginType.RESOURCE == plugin.type();
    }

    @Override
    public void handle(Plugin plugin) {
        URLClassLoader resourceClassLoader = null;
        try {
            resourceClassLoader = new URLClassLoader(plugin.dependencies(),
                    this.getClass().getClassLoader());

            Class<?> pluginClass = ClassUtils.forName(plugin.clazz(), resourceClassLoader);

            LOGGER.info("Register a new resource: {} [{}]", plugin.id(), pluginClass.getName());
            ResourcePluginImpl resource = new ResourcePluginImpl(plugin, pluginClass);
            resource.setConfiguration(new ResourceConfigurationClassFinder().lookupFirst(pluginClass, resourceClassLoader));
            resourcePluginManager.register(resource);
        } catch (Exception iae) {
            LOGGER.error("Unexpected error while creating resource instance", iae);
        } finally {
            if (resourceClassLoader != null) {
                try {
                    resourceClassLoader.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
