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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

@RequiredArgsConstructor
@Slf4j
class RepositoryTypeReader {

    private static final String REPOSITORIES_PREFIX = "repositories.";

    String getRepositoryType(Environment environment, Scope scope) {
        String oldKey = scope.getName() + ".type";
        String newKey = REPOSITORIES_PREFIX + oldKey;

        // Check old key first: it may come from an env var override (e.g. gravitee_management_type=jdbc)
        // which must take priority over the YAML-defined repositories.management.type value.
        String repositoryType = environment.getProperty(oldKey);
        if (repositoryType != null) {
            if (isExplicitlyDefined(environment, oldKey)) {
                log.warn(
                    "Repository for scope '{}' is configured using the deprecated key '{}'. Please migrate to '{}'.",
                    scope,
                    oldKey,
                    newKey
                );
            }
            return repositoryType;
        }

        return environment.getProperty(newKey);
    }

    /**
     * Checks if the key is explicitly defined in a property source (not via the alias).
     * This avoids false-positive deprecation warnings when the alias resolves the old key
     * from the new repositories.* format.
     */
    private boolean isExplicitlyDefined(Environment environment, String key) {
        if (environment instanceof ConfigurableEnvironment configEnv) {
            return configEnv
                .getPropertySources()
                .stream()
                .filter(ps -> !RepositoryAliasPropertySource.PROPERTY_SOURCE_NAME.equals(ps.getName()))
                .anyMatch(ps -> ps.getProperty(key) != null);
        }
        return false;
    }
}
