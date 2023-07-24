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
package io.gravitee.plugin.policy.internal;

import io.gravitee.plugin.core.api.AbstractSimplePluginHandler;
import io.gravitee.plugin.core.api.ConfigurablePluginManager;
import io.gravitee.plugin.core.api.Plugin;
import io.gravitee.plugin.policy.PolicyPlugin;
import io.gravitee.policy.api.annotations.OnRequest;
import io.gravitee.policy.api.annotations.OnRequestContent;
import io.gravitee.policy.api.annotations.OnResponse;
import io.gravitee.policy.api.annotations.OnResponseContent;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.reflections.ReflectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class PolicyPluginHandler extends AbstractSimplePluginHandler<PolicyPlugin> {

    @Autowired
    private ConfigurablePluginManager<PolicyPlugin> policyPluginManager;

    @Override
    public boolean canHandle(Plugin plugin) {
        return PolicyPlugin.PLUGIN_TYPE.equalsIgnoreCase(plugin.type());
    }

    @Override
    protected String type() {
        return "policies";
    }

    @Override
    protected PolicyPlugin create(Plugin plugin, Class<?> pluginClass) {
        PolicyPluginImpl policyPlugin = new PolicyPluginImpl(plugin, pluginClass);
        policyPlugin.setConfiguration(new PolicyConfigurationClassFinder().lookupFirst(pluginClass));
        policyPlugin.setContext(new PolicyContextClassFinder().lookupFirst(pluginClass, policyPlugin.policy().getClassLoader()));

        determineProxyPhases(pluginClass, policyPlugin);

        return policyPlugin;
    }

    private void determineProxyPhases(Class policyClass, PolicyPluginImpl entity) {
        if (!entity.manifest().properties().containsKey("proxy") && !entity.manifest().properties().containsKey("message")) {
            try {
                Set<String> proxyPhases = new HashSet<>();
                if (methodFoundFor(REQUEST_ANNOTATIONS, policyClass)) {
                    proxyPhases.add("REQUEST");
                }
                if (methodFoundFor(RESPONSE_ANNOTATIONS, policyClass)) {
                    proxyPhases.add("RESPONSE");
                }
                entity.manifest().properties().put("proxy", proxyPhases.stream().collect(Collectors.joining(",")));
            } catch (NoClassDefFoundError e) {
                // If the plugin use object that are only present in the GW classpath,
                // methodFoundFor will fail with ClassNotFound error
                // this shouldn't prevent the load of the plugin as the description of
                // proxy & message attributes in the manifest is only useful for APIM
                logger.debug("Unable to autodetect the execution phases for the plugin {}.", entity.id(), e);
            }
        }
    }

    private boolean methodFoundFor(Class<? extends Annotation>[] annotations, Class policyClass) {
        for (Class<? extends Annotation> annot : annotations) {
            Set<Method> resolved = ReflectionUtils.getAllMethods(
                policyClass,
                ReflectionUtils.withModifier(Modifier.PUBLIC),
                ReflectionUtils.withAnnotation(annot)
            );
            if (!resolved.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static final Class<? extends Annotation>[] REQUEST_ANNOTATIONS = new Class[] { OnRequest.class, OnRequestContent.class };
    private static final Class<? extends Annotation>[] RESPONSE_ANNOTATIONS = new Class[] { OnResponse.class, OnResponseContent.class };

    @Override
    protected void register(PolicyPlugin policyPlugin) {
        policyPluginManager.register(policyPlugin);

        // Once registered, the classloader should be released
        final ClassLoader policyClassLoader = policyPlugin.policy().getClassLoader();

        if (policyClassLoader instanceof URLClassLoader) {
            URLClassLoader classLoader = (URLClassLoader) policyClassLoader;
            try {
                classLoader.close();
            } catch (IOException e) {
                logger.error("Unexpected exception while trying to release the policy classloader");
            }
        }
    }

    @Override
    protected ClassLoader getClassLoader(Plugin plugin) {
        return new URLClassLoader(plugin.dependencies(), this.getClass().getClassLoader());
    }
}
