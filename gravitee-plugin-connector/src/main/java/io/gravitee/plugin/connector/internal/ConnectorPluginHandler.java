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
package io.gravitee.plugin.connector.internal;

import io.gravitee.plugin.connector.ConnectorPlugin;
import io.gravitee.plugin.connector.spring.ConnectorPluginConfiguration;
import io.gravitee.plugin.core.api.AbstractSimplePluginHandler;
import io.gravitee.plugin.core.api.ConfigurablePluginManager;
import io.gravitee.plugin.core.api.Plugin;
import java.io.IOException;
import java.net.URLClassLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@Import(ConnectorPluginConfiguration.class)
public class ConnectorPluginHandler extends AbstractSimplePluginHandler<ConnectorPlugin> {

    @Autowired
    private ConfigurablePluginManager<ConnectorPlugin> connectorPluginManager;

    @Override
    public boolean canHandle(Plugin plugin) {
        return ConnectorPlugin.PLUGIN_TYPE.equalsIgnoreCase(plugin.type());
    }

    @Override
    protected String type() {
        return "connectors";
    }

    @Override
    protected ConnectorPlugin create(Plugin plugin, Class<?> pluginClass) {
        ConnectorPluginImpl policyPlugin = new ConnectorPluginImpl(plugin, pluginClass);
        policyPlugin.setConfiguration(new ConnectorConfigurationClassFinder().lookupFirst(pluginClass));

        return policyPlugin;
    }

    @Override
    protected void register(ConnectorPlugin policyPlugin) {
        connectorPluginManager.register(policyPlugin);

        // Once registered, the classloader should be released
        // TODO: to check
        URLClassLoader classLoader = (URLClassLoader) policyPlugin.connector().getClassLoader();
        try {
            classLoader.close();
        } catch (IOException e) {
            logger.error("Unexpected exception while trying to release the policy classloader");
        }
    }

    @Override
    protected ClassLoader getClassLoader(Plugin plugin) {
        return new URLClassLoader(plugin.dependencies(), this.getClass().getClassLoader());
    }
}
