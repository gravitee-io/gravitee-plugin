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
package io.gravitee.plugin.integrationprovider.internal;

import io.gravitee.integration.api.plugin.configuration.IntegrationProviderConfiguration;
import io.gravitee.plugin.core.api.AbstractSingleSubTypesFinder;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Remi Baptiste (remi.baptiste at graviteesource.com)
 * @author GraviteeSource Team
 */
public class IntegrationProviderConfigurationClassFinder extends AbstractSingleSubTypesFinder<IntegrationProviderConfiguration> {

    private final Logger LOGGER = LoggerFactory.getLogger(IntegrationProviderConfigurationClassFinder.class);

    public IntegrationProviderConfigurationClassFinder() {
        super(IntegrationProviderConfiguration.class);
    }

    @Override
    public Collection<Class<? extends IntegrationProviderConfiguration>> lookup(Class clazz, ClassLoader classLoader) {
        LOGGER.debug(
            "Looking for a configuration class for integration provider {} in package {}",
            clazz.getName(),
            clazz.getPackage().getName()
        );
        Collection<Class<? extends IntegrationProviderConfiguration>> configurations = super.lookup(clazz, classLoader);

        if (configurations.isEmpty()) {
            LOGGER.info("No integration provider configuration class defined for integration provider {}", clazz.getName());
        }

        return configurations;
    }
}
