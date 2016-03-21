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
package io.gravitee.plugin.policy.impl;

import io.gravitee.plugin.core.api.Plugin;
import io.gravitee.plugin.core.api.PluginContextFactory;
import io.gravitee.plugin.core.api.PluginHandler;
import io.gravitee.plugin.policy.PolicyConfigurationClassResolver;
import io.gravitee.plugin.policy.PolicyDefinition;
import io.gravitee.plugin.policy.PolicyManager;
import io.gravitee.plugin.policy.PolicyMethodResolver;
import io.gravitee.policy.api.PolicyConfiguration;
import io.gravitee.policy.api.annotations.OnRequest;
import io.gravitee.policy.api.annotations.OnRequestContent;
import io.gravitee.policy.api.annotations.OnResponse;
import io.gravitee.policy.api.annotations.OnResponseContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author David BRASSELY (brasseld at gmail.com)
 * @author GraviteeSource Team
 */
public class PolicyPluginHandler implements PluginHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(PolicyPluginHandler.class);

    @Autowired
    private PluginContextFactory pluginContextFactory;

    @Autowired
    private PolicyMethodResolver policyMethodResolver;

    @Autowired
    private PolicyConfigurationClassResolver policyConfigurationClassResolver;

    @Autowired
    private PolicyManager policyManager;

    @Override
    public boolean canHandle(Plugin plugin) {
        return ! (policyMethodResolver.resolvePolicyMethods(plugin.clazz()).isEmpty());
    }

    @Override
    public void handle(Plugin plugin) {
        try {
            final Class<?> policyClass = plugin.clazz();
            LOGGER.info("Register a new policy: {} [{}]", plugin.id(), policyClass.getName());
            pluginContextFactory.create(plugin);

            Map<Class<? extends Annotation>, Method> methods = policyMethodResolver.resolvePolicyMethods(plugin.clazz());

            final Method onRequestMethod = methods.get(OnRequest.class);
            final Method onRequestContentMethod = methods.get(OnRequestContent.class);
            final Method onResponseMethod = methods.get(OnResponse.class);
            final Method onResponseContentMethod = methods.get(OnResponseContent.class);

            if (onRequestMethod == null && onResponseMethod == null && onResponseContentMethod == null &&
                    onRequestContentMethod == null) {
                LOGGER.error("No method annotated with @OnRequest / @OnResponse / @OnRequestContent / @OnResponseContent" +
                        " found, skip policy registration for {}", policyClass.getName());
            } else {
                final Class<? extends PolicyConfiguration> policyConfiguration = policyConfigurationClassResolver.resolvePolicyConfigurationClass(plugin.clazz());

                PolicyDefinition definition = new PolicyDefinition() {
                    @Override
                    public String id() {
                        return plugin.id();
                    }

                    @Override
                    public Class<?> policy() {
                        return policyClass;
                    }

                    @Override
                    public Class<? extends PolicyConfiguration> configuration() {
                        return policyConfiguration;
                    }

                    @Override
                    public Method onRequestMethod() {
                        return onRequestMethod;
                    }

                    @Override
                    public Method onRequestContentMethod() {
                        return onRequestContentMethod;
                    }

                    @Override
                    public Method onResponseMethod() {
                        return onResponseMethod;
                    }

                    @Override
                    public Method onResponseContentMethod() {
                        return onResponseContentMethod;
                    }

                    @Override
                    public Plugin plugin() {
                        return plugin;
                    }
                };

                policyManager.registerPolicyDefinition(definition);
            }
        } catch (Exception iae) {
            LOGGER.error("Unexpected error while create reporter instance", iae);

            pluginContextFactory.remove(plugin);
        }
    }

    public void setPolicyMethodResolver(PolicyMethodResolver policyMethodResolver) {
        this.policyMethodResolver = policyMethodResolver;
    }

    public void setPluginContextFactory(PluginContextFactory pluginContextFactory) {
        this.pluginContextFactory = pluginContextFactory;
    }
}
