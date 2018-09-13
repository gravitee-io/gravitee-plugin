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
package io.gravitee.plugin.alert.internal;

import io.gravitee.alert.api.service.Alert;
import io.gravitee.plugin.alert.AlertEngineService;
import io.gravitee.plugin.core.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

/**
 * @author Azize ELAMRANI (azize.elamrani at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AlertPluginHandler implements PluginHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertPluginHandler.class);

    @Autowired
    private Environment environment;
    @Autowired
    private PluginContextFactory pluginContextFactory;
    @Autowired
    private PluginClassLoaderFactory pluginClassLoaderFactory;
    @Autowired
    private AlertEngineService alertService;

    @Override
    public boolean canHandle(Plugin plugin) {
        return PluginType.ALERT == plugin.type();
    }

    @Override
    public void handle(Plugin plugin) {
        LOGGER.info("Register a new alert: {} [{}]", plugin.id(), plugin.clazz());
        boolean enabled = isEnabled(plugin);
        if (enabled) {
            try {
                pluginClassLoaderFactory.getOrCreateClassLoader(plugin, this.getClass().getClassLoader());
                final ApplicationContext context = pluginContextFactory.create(plugin);
                final Alert alert = context.getBean(Alert.class);
                alertService.register(alert);
            } catch (Exception iae) {
                LOGGER.error("Unexpected error while create alert instance", iae);
                // Be sure that the context does not exist anymore.
                pluginContextFactory.remove(plugin);
            }
        } else {
            LOGGER.warn("Plugin {} is disabled. Please have a look to your configuration to re-enable it", plugin.id());
        }
    }

    private boolean isEnabled(Plugin alertPlugin) {
        boolean enabled = environment.getProperty(alertPlugin.id() + ".enabled", Boolean.class, false);
        LOGGER.debug("Plugin {} configuration: {}", alertPlugin.id(), enabled);
        return enabled;
    }
}
