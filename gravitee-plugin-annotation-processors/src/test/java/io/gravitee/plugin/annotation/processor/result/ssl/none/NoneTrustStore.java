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
package io.gravitee.plugin.annotation.processor.result.ssl.none;

import io.gravitee.plugin.annotation.processor.result.ssl.TrustStore;
import io.gravitee.plugin.annotation.processor.result.ssl.TrustStoreType;
import java.io.Serial;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class NoneTrustStore extends TrustStore {

    @Serial
    private static final long serialVersionUID = -6013813999148592319L;

    public NoneTrustStore() {
        super(TrustStoreType.NONE);
    }

    public static NoneTrustStoreBuilder builder() {
        return new NoneTrustStoreBuilder();
    }

    public static class NoneTrustStoreBuilder {

        NoneTrustStoreBuilder() {}

        public NoneTrustStore build() {
            return new NoneTrustStore();
        }

        public String toString() {
            return "NoneTrustStore.NoneTrustStoreBuilder()";
        }
    }
}
