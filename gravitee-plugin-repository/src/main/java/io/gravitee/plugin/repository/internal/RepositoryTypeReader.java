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
import org.springframework.core.env.Environment;

@RequiredArgsConstructor
@Slf4j
class RepositoryTypeReader {

    String getRepositoryType(Environment environment, Scope scope) {
        String oldKey = scope.getName() + ".type";
        String newKey = "repositories." + oldKey;
        String repositoryType = environment.getProperty(newKey);
        if (repositoryType != null) {
            return repositoryType;
        } else {
            repositoryType = environment.getProperty(oldKey);
            if (repositoryType != null) {
                log.warn("The repository of scope {} is loaded from discouraged section '{}'. Please use '{}'.", scope, oldKey, newKey);
            }
            return repositoryType;
        }
    }
}
