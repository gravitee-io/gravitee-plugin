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
package io.gravitee.plugin.policy.internal;

import io.gravitee.plugin.core.api.AbstractSimplePluginHandler;
import io.gravitee.plugin.core.api.ConfigurablePluginManager;
import io.gravitee.plugin.core.api.Plugin;
import io.gravitee.plugin.core.api.PluginType;
import io.gravitee.plugin.policy.PolicyPlugin;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URLClassLoader;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class PolicyPluginHandler extends AbstractSimplePluginHandler<PolicyPlugin> {

    @Autowired
    private ConfigurablePluginManager<PolicyPlugin> policyPluginManager;

    @Override
    public boolean canHandle(Plugin plugin) {
        return plugin.type() == PluginType.POLICY;
    }

    @Override
    protected String type() {
        return "policies";
    }

    @Override
    protected PolicyPlugin create(Plugin plugin, Class<?> pluginClass) {
        PolicyPluginImpl policyPlugin = new PolicyPluginImpl(plugin, pluginClass);
        policyPlugin.setConfiguration(new PolicyConfigurationClassFinder().lookupFirst(pluginClass));

        return policyPlugin;
    }

    @Override
    protected void register(PolicyPlugin policyPlugin) {
        policyPluginManager.register(policyPlugin);

        // Once registered, the classloader should be released
        URLClassLoader classLoader = (URLClassLoader) policyPlugin.policy().getClassLoader();
        try {
            classLoader.close();
        } catch (IOException e) {
            logger.error("Unexpected exception while trying to release the policy classloader");
        }
    }

    @Override
    protected ClassLoader getClassLoader(Plugin plugin) {
        return new URLClassLoader(plugin.dependencies(),
                this.getClass().getClassLoader());
    }
}
