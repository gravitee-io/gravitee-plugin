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

import io.gravitee.plugin.core.api.ConfigurablePluginManager;
import io.gravitee.plugin.core.api.Plugin;
import io.gravitee.plugin.core.api.PluginHandler;
import io.gravitee.plugin.core.api.PluginType;
import io.gravitee.plugin.policy.PolicyPlugin;
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
public class PolicyPluginHandler implements PluginHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(PolicyPluginHandler.class);

    @Autowired
    private ConfigurablePluginManager<PolicyPlugin> policyPluginManager;

    @Override
    public boolean canHandle(Plugin plugin) {
        return plugin.type() == PluginType.POLICY;
    }

    @Override
    public void handle(Plugin plugin) {
        URLClassLoader policyClassLoader = null;
        try {
            policyClassLoader = new URLClassLoader(plugin.dependencies(),
                    this.getClass().getClassLoader());

            Class<?> pluginClass = ClassUtils.forName(plugin.clazz(), policyClassLoader);

            LOGGER.info("Register a new policy: {} [{}]", plugin.id(), pluginClass.getName());
            PolicyPluginImpl policy = new PolicyPluginImpl(plugin, pluginClass);
            policy.setConfiguration(new PolicyConfigurationClassFinder().lookupFirst(pluginClass, policyClassLoader));

            policyPluginManager.register(policy);
        } catch (Exception iae) {
            LOGGER.error("Unexpected error while creating policy instance", iae);
        } finally {
            if (policyClassLoader != null) {
                try {
                    policyClassLoader.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
