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
package io.gravitee.plugin.cloudserviceprovider.internal;

import io.gravitee.cloudservice.api.plugin.configuration.CloudServiceProviderConfiguration;
import io.gravitee.plugin.core.api.AbstractSingleSubTypesFinder;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Remi Baptiste (remi.baptiste at graviteesource.com)
 * @author GraviteeSource Team
 */
public class CloudServiceProviderConfigurationClassFinder extends AbstractSingleSubTypesFinder<CloudServiceProviderConfiguration> {

    private final Logger LOGGER = LoggerFactory.getLogger(CloudServiceProviderConfigurationClassFinder.class);

    public CloudServiceProviderConfigurationClassFinder() {
        super(CloudServiceProviderConfiguration.class);
    }

    @Override
    public Collection<Class<? extends CloudServiceProviderConfiguration>> lookup(Class clazz, ClassLoader classLoader) {
        LOGGER.debug(
            "Looking for a configuration class for cloud service provider {} in package {}",
            clazz.getName(),
            clazz.getPackage().getName()
        );
        Collection<Class<? extends CloudServiceProviderConfiguration>> configurations = super.lookup(clazz, classLoader);

        if (configurations.isEmpty()) {
            LOGGER.info("No cloud service provider configuration class defined for cloud service provider {}", clazz.getName());
        }

        return configurations;
    }
}
