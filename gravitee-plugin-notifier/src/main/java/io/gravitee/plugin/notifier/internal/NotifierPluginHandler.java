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

import io.gravitee.plugin.core.api.AbstractSimplePluginHandler;
import io.gravitee.plugin.core.api.ConfigurablePluginManager;
import io.gravitee.plugin.core.api.Plugin;
import io.gravitee.plugin.notifier.NotifierPlugin;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URLClassLoader;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class NotifierPluginHandler extends AbstractSimplePluginHandler<NotifierPlugin> {

    @Autowired
    private ConfigurablePluginManager<NotifierPlugin> notifierPluginManager;

    @Override
    public boolean canHandle(Plugin plugin) {
        return NotifierPlugin.PLUGIN_TYPE.equalsIgnoreCase(plugin.type());
    }

    @Override
    protected String type() {
        return "notifiers";
    }

    @Override
    protected NotifierPlugin create(Plugin plugin, Class<?> pluginClass) {
        NotifierPluginImpl notifierPlugin = new NotifierPluginImpl(plugin, pluginClass);
        notifierPlugin.setConfiguration(new NotifierResourceConfigurationClassFinder().lookupFirst(pluginClass));

        return notifierPlugin;
    }

    @Override
    protected void register(NotifierPlugin plugin) {
        notifierPluginManager.register(plugin);

        // Once registered, the classloader should be released
        URLClassLoader classLoader = (URLClassLoader) plugin.notifier().getClassLoader();
        try {
            classLoader.close();
        } catch (IOException e) {
            logger.error("Unexpected exception while trying to release the notifier classloader");
        }
    }

    @Override
    protected ClassLoader getClassLoader(Plugin plugin) throws Exception {
        return new URLClassLoader(plugin.dependencies(),
                this.getClass().getClassLoader());
    }
}
