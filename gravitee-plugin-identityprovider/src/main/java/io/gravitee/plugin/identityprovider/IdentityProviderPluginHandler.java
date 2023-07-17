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
package io.gravitee.plugin.identityprovider;

import io.gravitee.identityprovider.api.IdentityProvider;
import io.gravitee.identityprovider.api.IdentityProviderManager;
import io.gravitee.plugin.core.api.*;
import io.gravitee.plugin.core.internal.AnnotationBasedPluginContextConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class IdentityProviderPluginHandler extends AbstractPluginHandler implements PluginHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityProviderPluginHandler.class);

    @Autowired
    private PluginClassLoaderFactory<Plugin> pluginClassLoaderFactory;

    @Autowired
    private PluginContextFactory pluginContextFactory;

    @Autowired
    private IdentityProviderManager identityProviderManager;

    @Override
    public boolean canHandle(Plugin plugin) {
        return plugin.type().equalsIgnoreCase(PluginType.IDENTITY_PROVIDER.name());
    }

    @Override
    protected String type() {
        return PluginType.IDENTITY_PROVIDER.name().toLowerCase();
    }

    @Override
    protected ClassLoader getClassLoader(Plugin plugin) {
        return pluginClassLoaderFactory.getOrCreateClassLoader(plugin, this.getClass().getClassLoader());
    }

    @Override
    protected void handle(Plugin plugin, Class<?> pluginClass) {
        try {
            LOGGER.info("Register a new identity provider plugin: {} [{}]", plugin.id(), plugin.clazz());

            Assert.isAssignable(IdentityProvider.class, pluginClass);

            ApplicationContext repoApplicationContext = pluginContextFactory.create(new AnnotationBasedPluginContextConfigurer(plugin));

            IdentityProvider idpClassInstance = repoApplicationContext.getBean((Class<IdentityProvider>) pluginClass);

            identityProviderManager.put(idpClassInstance.getSource(), idpClassInstance);
        } catch (Exception iae) {
            LOGGER.error("Unexpected error while creating identity provider instance", iae);
            pluginContextFactory.remove(plugin);
        }
    }
}
