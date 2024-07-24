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
package io.gravitee.plugin.datasource.internal;

import io.gravitee.datasource.api.DatasourceConfiguration;
import io.gravitee.plugin.core.api.AbstractSingleSubTypesFinder;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Slf4j
public class DatasourceConfigurationClassFinder extends AbstractSingleSubTypesFinder<DatasourceConfiguration> {

    public DatasourceConfigurationClassFinder() {
        super(DatasourceConfiguration.class);
    }

    @Override
    public Collection<Class<? extends DatasourceConfiguration>> lookup(Class clazz, ClassLoader classLoader) {
        log.debug("Looking for a configuration class for datasource {} in package {}", clazz.getName(), clazz.getPackage().getName());
        Collection<Class<? extends DatasourceConfiguration>> configurations = super.lookup(clazz, classLoader);

        if (configurations.isEmpty()) {
            log.info("No configuration class defined for datasource {}", clazz.getName());
        }

        return configurations;
    }
}
