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
import io.gravitee.policy.api.PolicyContext;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author David BRASSELY (david at graviteesource.com)
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class PolicyContextClassFinder extends AbstractSingleSubTypesFinder<PolicyContext> {

    private final Logger LOGGER = LoggerFactory.getLogger(PolicyContextClassFinder.class);

    public PolicyContextClassFinder() {
        super(PolicyContext.class);
    }

    @Override
    public Collection<Class<? extends PolicyContext>> lookup(Class clazz, ClassLoader classLoader) {
        LOGGER.debug("Looking for a context class for policy {} in package {}", clazz.getName(), clazz.getPackage().getName());
        Collection<Class<? extends PolicyContext>> classes = super.lookup(clazz, classLoader);

        if (classes.isEmpty()) {
            LOGGER.debug("No policy context class defined for policy {}", clazz.getName());
        }

        return classes;
    }
}
