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
package io.gravitee.plugin.discovery.internal;

import io.gravitee.discovery.api.ServiceDiscoveryConfiguration;
import io.gravitee.plugin.core.api.AbstractSingleSubTypesFinder;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class ServiceDiscoveryConfigurationClassFinder extends AbstractSingleSubTypesFinder<ServiceDiscoveryConfiguration> {

    private final Logger LOGGER = LoggerFactory.getLogger(ServiceDiscoveryConfigurationClassFinder.class);

    public ServiceDiscoveryConfigurationClassFinder() {
        super(ServiceDiscoveryConfiguration.class);
    }

    @Override
    public Collection<Class<? extends ServiceDiscoveryConfiguration>> lookup(Class clazz, ClassLoader classLoader) {
        LOGGER.debug(
            "Looking for a configuration class for service discovery {} in package {}",
            clazz.getName(),
            clazz.getPackage().getName()
        );
        Collection<Class<? extends ServiceDiscoveryConfiguration>> configurations = super.lookup(clazz, classLoader);

        if (configurations.isEmpty()) {
            LOGGER.info("No service discovery configuration class defined for service discovery {}", clazz.getName());
        }

        return configurations;
    }
}
