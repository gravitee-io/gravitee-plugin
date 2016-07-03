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
package io.gravitee.plugin.fetcher.internal;

import io.gravitee.fetcher.api.FetcherConfiguration;
import io.gravitee.plugin.core.api.AbstractSingleSubTypesFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
class FetcherConfigurationClassFinder extends AbstractSingleSubTypesFinder<FetcherConfiguration> {

    private final Logger LOGGER = LoggerFactory.getLogger(FetcherConfigurationClassFinder.class);

    FetcherConfigurationClassFinder() {
        super(FetcherConfiguration.class);
    }

    @Override
    public Collection<Class<? extends FetcherConfiguration>> lookup(Class clazz, ClassLoader classLoader) {
        LOGGER.debug("Looking for a configuration class for fetcher {} in package {}",
                clazz.getName(), clazz.getPackage().getName());
        Collection<Class<? extends FetcherConfiguration>> configurations = super.lookup(clazz, classLoader);

        if (configurations.isEmpty()) {
            LOGGER.info("No resource configuration class defined for fetcher {}", clazz.getName());
        }

        return configurations;
    }
}
