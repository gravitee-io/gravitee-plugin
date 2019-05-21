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
package io.gravitee.plugin.notifier.internal;

import io.gravitee.plugin.core.api.ConfigurablePluginManager;
import io.gravitee.plugin.core.api.Plugin;
import io.gravitee.plugin.core.api.PluginHandler;
import io.gravitee.plugin.core.api.PluginType;
import io.gravitee.plugin.notifier.NotifierPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.net.URLClassLoader;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class NotifierPluginHandler implements PluginHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(NotifierPluginHandler.class);

    @Autowired
    private ConfigurablePluginManager<NotifierPlugin> notifierPluginManager;

    @Override
    public boolean canHandle(Plugin plugin) {
        return PluginType.NOTIFIER == plugin.type();
    }

    @Override
    public void handle(Plugin plugin) {
        URLClassLoader notifierClassLoader = null;
        try {
            notifierClassLoader = new URLClassLoader(plugin.dependencies(),
                    this.getClass().getClassLoader());

            Class<?> pluginClass = ClassUtils.forName(plugin.clazz(), notifierClassLoader);

            LOGGER.info("Register a new notifier: {} [{}]", plugin.id(), pluginClass.getName());
            NotifierPluginImpl notifier = new NotifierPluginImpl(plugin, pluginClass);
            notifier.setConfiguration(new NotifierResourceConfigurationClassFinder().lookupFirst(pluginClass, notifierClassLoader));
            notifierPluginManager.register(notifier);
        } catch (Exception iae) {
            LOGGER.error("Unexpected error while creating notifier instance", iae);
        } finally {
            if (notifierClassLoader != null) {
                try {
                    notifierClassLoader.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
