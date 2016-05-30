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

import io.gravitee.plugin.core.api.AbstractSingleSubTypesFinder;
import io.gravitee.policy.api.PolicyConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * @author David BRASSELY (david at gravitee.io)
 * @author GraviteeSource Team
 */
public class PolicyConfigurationClassFinder extends AbstractSingleSubTypesFinder<PolicyConfiguration> {

    private final Logger LOGGER = LoggerFactory.getLogger(PolicyConfigurationClassFinder.class);

    public PolicyConfigurationClassFinder() {
        super(PolicyConfiguration.class);
    }

    @Override
    public Collection<Class<? extends PolicyConfiguration>> lookup(Class clazz, ClassLoader classLoader) {
        LOGGER.debug("Looking for a configuration class for policy {} in package {}",
                clazz.getName(), clazz.getPackage().getName());
        Collection<Class<? extends PolicyConfiguration>> configurations = super.lookup(clazz, classLoader);

        if (configurations.isEmpty()) {
            LOGGER.info("No policy configuration class defined for policy {}", clazz.getName());
        }

        return configurations;
    }
}
