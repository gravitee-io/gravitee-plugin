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
package io.gravitee.plugin.core.api;

import io.gravitee.plugin.api.DeploymentContextFactory;
import io.gravitee.plugin.api.DeploymentLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.net.URLClassLoader;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public abstract class AbstractPluginHandler implements PluginHandler {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Environment environment;

    @Autowired
    private DeploymentContextFactory deploymentContextFactory;

    @Override
    public void handle(Plugin plugin) {
        if (isEnabled(plugin)) {
            logger.info("Install plugin: {} [{}]", plugin.id(), plugin.clazz());

            ClassLoader classloader = null;

            try {
                classloader = getClassLoader(plugin);

                final Class<?> pluginClass = classloader.loadClass(plugin.clazz());

                io.gravitee.plugin.api.annotations.Plugin ann = pluginClass.getAnnotation(io.gravitee.plugin.api.annotations.Plugin.class);

                if (ann != null) {
                    Class<? extends DeploymentLifecycle> deploymentClass = ann.deployment();

                    // Load deployment lifecycle implementation from plugin classloader
                    DeploymentLifecycle deploymentLifecycle = deploymentClass.getDeclaredConstructor().newInstance();

                    deploymentLifecycle.onDeploy(deploymentContextFactory.create());
                }

                handle(plugin, pluginClass);
            } catch (Throwable t) {
                logger.error("An error occurs while installing plugin: {} [{}]", plugin.id(), plugin.clazz(), t);
                if (classloader instanceof URLClassLoader) {
                    try {
                        ((URLClassLoader) classloader).close();
                    } catch (IOException e) {
                    }
                }
                if (classloader instanceof PluginClassLoader) {
                    try {
                        ((PluginClassLoader) classloader).close();
                    } catch (IOException e) {
                    }
                }
            }
        } else {
            logger.info("Installation skipped for: {} [{}]", plugin.id(), plugin.clazz());
        }
    }

    private boolean isEnabled(Plugin plugin) {
        boolean enabled = environment.getProperty(type() + '.' + plugin.id() + ".enabled", Boolean.class, true);
        logger.debug("Plugin {} is enabled: {}", plugin.id(), enabled);
        return enabled;
    }

    protected abstract String type();

    protected abstract ClassLoader getClassLoader(Plugin plugin) throws Exception;

    protected abstract void handle(Plugin plugin, Class<?> pluginClass);
}
