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
package io.gravitee.plugin.cloudserviceprovider.internal;

import io.gravitee.plugin.cloudserviceprovider.CloudServiceProviderPlugin;
import io.gravitee.plugin.cloudserviceprovider.spring.CloudServiceProviderPluginConfiguration;
import io.gravitee.plugin.core.api.AbstractSimplePluginHandler;
import io.gravitee.plugin.core.api.Plugin;
import java.io.IOException;
import java.net.URLClassLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * @author Remi Baptiste (remi.baptiste at graviteesource.com)
 * @author GraviteeSource Team
 */
@Import(CloudServiceProviderPluginConfiguration.class)
public class CloudServiceProviderPluginHandler extends AbstractSimplePluginHandler<CloudServiceProviderPlugin> {

    @Autowired
    private DefaultCloudServiceProviderPluginManager cloudServiceProviderPluginManager;

    @Override
    public boolean canHandle(Plugin plugin) {
        return CloudServiceProviderPlugin.PLUGIN_TYPE.equalsIgnoreCase(plugin.type());
    }

    @Override
    protected String type() {
        return "cloudservice-providers";
    }

    @Override
    protected CloudServiceProviderPlugin create(Plugin plugin, Class<?> pluginClass) {
        DefaultCloudServiceProviderPlugin cloudServiceProviderPlugin = new DefaultCloudServiceProviderPlugin(plugin, pluginClass);
        cloudServiceProviderPlugin.setConfiguration(new CloudServiceProviderConfigurationClassFinder().lookupFirst(pluginClass));

        return cloudServiceProviderPlugin;
    }

    @Override
    protected void register(CloudServiceProviderPlugin cloudServiceProviderPlugin) {
        cloudServiceProviderPluginManager.register(cloudServiceProviderPlugin);

        // Once registered, the classloader should be released
        URLClassLoader classLoader = (URLClassLoader) cloudServiceProviderPlugin.cloudServiceProvider().getClassLoader();
        try {
            classLoader.close();
        } catch (IOException e) {
            logger.error("Unexpected exception while trying to release the cloud service provider classloader");
        }
    }

    @Override
    protected ClassLoader getClassLoader(Plugin plugin) {
        return new URLClassLoader(plugin.dependencies(), this.getClass().getClassLoader());
    }
}
