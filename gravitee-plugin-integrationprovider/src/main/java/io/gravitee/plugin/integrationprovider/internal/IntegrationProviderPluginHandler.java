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
package io.gravitee.plugin.integrationprovider.internal;

import io.gravitee.plugin.core.api.AbstractSimplePluginHandler;
import io.gravitee.plugin.core.api.Plugin;
import io.gravitee.plugin.integrationprovider.IntegrationProviderPlugin;
import io.gravitee.plugin.integrationprovider.spring.IntegrationProviderPluginConfiguration;
import java.io.IOException;
import java.net.URLClassLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * @author Remi Baptiste (remi.baptiste at graviteesource.com)
 * @author GraviteeSource Team
 */
@Import(IntegrationProviderPluginConfiguration.class)
public class IntegrationProviderPluginHandler extends AbstractSimplePluginHandler<IntegrationProviderPlugin> {

    @Autowired
    private DefaultIntegrationProviderPluginManager integrationProviderPluginManager;

    @Override
    public boolean canHandle(Plugin plugin) {
        return IntegrationProviderPlugin.PLUGIN_TYPE.equalsIgnoreCase(plugin.type());
    }

    @Override
    protected String type() {
        return "integration-providers";
    }

    @Override
    protected IntegrationProviderPlugin create(Plugin plugin, Class<?> pluginClass) {
        DefaultIntegrationProviderPlugin integrationProviderPlugin = new DefaultIntegrationProviderPlugin(plugin, pluginClass);
        integrationProviderPlugin.setConfiguration(new IntegrationProviderConfigurationClassFinder().lookupFirst(pluginClass));

        return integrationProviderPlugin;
    }

    @Override
    protected void register(IntegrationProviderPlugin integrationProviderPlugin) {
        integrationProviderPluginManager.register(integrationProviderPlugin);

        // Once registered, the classloader should be released
        URLClassLoader classLoader = (URLClassLoader) integrationProviderPlugin.integrationProvider().getClassLoader();
        try {
            classLoader.close();
        } catch (IOException e) {
            logger.error("Unexpected exception while trying to release the integration provider classloader");
        }
    }

    @Override
    protected ClassLoader getClassLoader(Plugin plugin) {
        return new URLClassLoader(plugin.dependencies(), this.getClass().getClassLoader());
    }
}
