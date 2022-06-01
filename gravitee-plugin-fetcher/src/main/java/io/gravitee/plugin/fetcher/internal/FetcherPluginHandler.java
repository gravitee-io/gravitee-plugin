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
package io.gravitee.plugin.fetcher.internal;

import io.gravitee.plugin.core.api.AbstractSimplePluginHandler;
import io.gravitee.plugin.core.api.ConfigurablePluginManager;
import io.gravitee.plugin.core.api.Plugin;
import io.gravitee.plugin.fetcher.FetcherPlugin;
import java.net.URLClassLoader;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class FetcherPluginHandler extends AbstractSimplePluginHandler<FetcherPlugin> {

    @Autowired
    private ConfigurablePluginManager<FetcherPlugin> fetcherPluginManager;

    @Override
    public boolean canHandle(Plugin plugin) {
        return FetcherPlugin.PLUGIN_TYPE.equalsIgnoreCase(plugin.type());
    }

    @Override
    protected String type() {
        return null;
    }

    @Override
    protected ClassLoader getClassLoader(Plugin plugin) throws Exception {
        return new URLClassLoader(plugin.dependencies(), this.getClass().getClassLoader());
    }

    @Override
    protected FetcherPlugin create(Plugin plugin, Class<?> pluginClass) {
        FetcherPluginImpl fetcher = new FetcherPluginImpl(plugin, pluginClass);
        fetcher.setConfiguration(new FetcherConfigurationClassFinder().lookupFirst(pluginClass));

        return fetcher;
    }

    @Override
    protected void register(FetcherPlugin plugin) {
        fetcherPluginManager.register(plugin);
    }
}
