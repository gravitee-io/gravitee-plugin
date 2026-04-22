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
package io.gravitee.plugin.repository.internal;

import io.gravitee.platform.repository.api.Scope;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

/**
 * A {@link PropertySource} that transparently resolves legacy repository configuration keys
 * to the new hierarchical format.
 *
 * <p>When application code reads a property like {@code management.mongodb.uri}, this source
 * first checks for {@code repositories.management.mongodb.uri}. If found, that value is returned.
 * If not, {@code null} is returned and the normal property resolution chain handles the legacy key.
 */
class RepositoryAliasPropertySource extends PropertySource<Object> {

    static final String PROPERTY_SOURCE_NAME = "repositoryAliasPropertySource";
    private static final String REPOSITORIES_PREFIX = "repositories.";

    private static final Set<String> SCOPE_PREFIXES = Arrays.stream(Scope.values())
        .map(scope -> scope.getName() + ".")
        .collect(Collectors.toSet());

    private final Environment environment;

    RepositoryAliasPropertySource(Environment environment) {
        super(PROPERTY_SOURCE_NAME, new Object());
        this.environment = environment;
    }

    @Override
    public Object getProperty(@NonNull String name) {
        for (String scopePrefix : SCOPE_PREFIXES) {
            if (name.startsWith(scopePrefix)) {
                return resolveRespectingSourcePriority(name);
            }
        }
        return null;
    }

    /**
     * Resolves the property by checking the legacy key first (explicit override)
     * across all sources, then falling back to the new key.
     */
    private Object resolveRespectingSourcePriority(String name) {
        if (environment instanceof ConfigurableEnvironment configEnv) {
            // Check if any source defines the legacy key directly (e.g. env var)
            for (PropertySource<?> ps : configEnv.getPropertySources()) {
                if (ps == this) {
                    continue;
                }
                Object value = ps.getProperty(name);
                if (value != null) {
                    return value;
                }
            }
        }
        // Fallback to new key
        return environment.getProperty(REPOSITORIES_PREFIX + name);
    }
}
