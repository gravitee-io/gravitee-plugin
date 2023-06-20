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
package io.gravitee.plugin.repository.internal;

import io.gravitee.platform.repository.api.RepositoryProvider;
import io.gravitee.platform.repository.api.RepositoryScopeProvider;
import io.gravitee.platform.repository.api.Scope;
import io.gravitee.plugin.core.api.*;
import io.gravitee.plugin.core.internal.AnnotationBasedPluginContextConfigurer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class RepositoryPluginHandler extends AbstractPluginHandler implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryPluginHandler.class);
    public static final int RETRY_DELAY_MS = 5000;

    private static final String PLUGIN_TYPE = "repository";

    @Autowired
    private Environment environment;

    @Autowired
    private PluginContextFactory pluginContextFactory;

    @Autowired
    private PluginClassLoaderFactory<Plugin> pluginClassLoaderFactory;

    @Autowired
    private ApplicationContext applicationContext;

    private final Map<Scope, RepositoryProvider> repositories = new HashMap<>();

    @Override
    public void afterPropertiesSet() {
        RepositoryScopeProvider scopeProvider = applicationContext.getBean(RepositoryScopeProvider.class);

        // Get all the scope handled by the plugin and check there is an associated configuration.
        for (Scope scope : scopeProvider.getHandledScopes()) {
            checkRepositoryConfig(scope, true);
        }

        for (Scope scope : scopeProvider.getOptionalHandledScopes()) {
            checkRepositoryConfig(scope, false);
        }
    }

    @Override
    public boolean canHandle(Plugin plugin) {
        return PLUGIN_TYPE.equalsIgnoreCase(plugin.type());
    }

    @Override
    protected String type() {
        return PLUGIN_TYPE;
    }

    @Override
    protected ClassLoader getClassLoader(Plugin plugin) {
        return pluginClassLoaderFactory.getOrCreateClassLoader(plugin, this.getClass().getClassLoader());
    }

    @Override
    protected void handle(Plugin plugin, Class<?> repositoryClass) {
        if (plugin.deployed()) {
            try {
                LOGGER.info("Register a new repository: {} [{}]", plugin.id(), plugin.clazz());

                Assert.isAssignable(RepositoryProvider.class, repositoryClass);

                RepositoryProvider repository = createInstance((Class<RepositoryProvider>) repositoryClass);
                Scope[] scopes = repository.scopes();

                for (Scope scope : scopes) {
                    if (repository.type().equals(getRepositoryType(scope))) {
                        if (!repositories.containsKey(scope)) {
                            boolean loaded = false;
                            int tries = 0;

                            while (!loaded) {
                                if (tries > 0) {
                                    // Wait some time before giving an other try.
                                    Thread.sleep(RETRY_DELAY_MS);
                                }
                                loaded = loadRepository(scope, repository, plugin);
                                tries++;

                                if (!loaded) {
                                    LOGGER.error(
                                        "Unable to load repository {} for scope {}. Retry in {} ms...",
                                        plugin.id(),
                                        scope,
                                        RETRY_DELAY_MS
                                    );
                                }
                            }
                        } else {
                            LOGGER.warn("Repository scope {} already loaded by {}", scope, repositories.get(scope));
                        }
                    }
                }
            } catch (Exception iae) {
                LOGGER.error("Unexpected error while create repository instance", iae);
            }
        }
    }

    private boolean loadRepository(Scope scope, RepositoryProvider repository, Plugin plugin) {
        LOGGER.info("Repository [{}] loaded by {}", scope, repository.type());

        // Not yet loaded, let's mount the repository in application context
        try {
            ApplicationContext repoApplicationContext = pluginContextFactory.create(
                new AnnotationBasedPluginContextConfigurer(plugin) {
                    @Override
                    public Set<Class<?>> configurations() {
                        return Collections.singleton(repository.configuration(scope));
                    }
                }
            );

            registerRepositoryDefinitions(repository, repoApplicationContext);
            repositories.put(scope, repository);
            return true;
        } catch (Exception iae) {
            LOGGER.error("Unexpected error while creating context for repository instance", iae);
            pluginContextFactory.remove(plugin);

            return false;
        }
    }

    private void registerRepositoryDefinitions(RepositoryProvider repository, ApplicationContext repoApplicationContext) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) (
            (ConfigurableApplicationContext) applicationContext
        ).getBeanFactory();

        String[] beanNames = repoApplicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object repositoryClassInstance = repoApplicationContext.getBean(beanName);
            Class<?> repositoryObjectClass = repositoryClassInstance.getClass();
            if (
                (
                    beanName.endsWith("Repository") ||
                    beanName.endsWith("TransactionManager") &&
                    !repository.getClass().equals(repositoryClassInstance.getClass())
                )
            ) {
                if (repositoryObjectClass.getInterfaces().length > 0) {
                    beanFactory.registerSingleton(beanName, repositoryClassInstance);
                }
            }
        }
    }

    /**
     * Check the repository configuration is defined for the corresponding scope.
     * @param scope the scope for which to check the configuration.
     * @param failOnMissing if <code>true</code> fail if missing configuration
     *
     * @throws IllegalStateException thrown if no repository configuration has been found for the scope.
     */
    private void checkRepositoryConfig(final Scope scope, final boolean failOnMissing) throws IllegalStateException {
        String repositoryType = getRepositoryType(scope);
        LOGGER.info("Loading repository for scope {}: {}", scope, repositoryType);

        if (repositoryType == null || repositoryType.isEmpty()) {
            if (failOnMissing) {
                LOGGER.error("No repository type defined in configuration for {}", scope.getName());
                throw new IllegalStateException("No repository type defined in configuration for " + scope.getName());
            } else {
                LOGGER.warn("No repository type defined in configuration for {}", scope.getName());
            }
        }
    }

    private String getRepositoryType(Scope scope) {
        return environment.getProperty(scope.getName() + ".type");
    }

    private <T> T createInstance(Class<T> clazz) throws Exception {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            LOGGER.error("Unable to instantiate class: {}", clazz.getName());
            throw ex;
        }
    }
}
