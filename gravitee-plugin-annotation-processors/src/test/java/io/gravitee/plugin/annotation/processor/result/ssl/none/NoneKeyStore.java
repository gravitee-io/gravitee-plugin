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

import io.gravitee.plugin.annotation.processor.result.ssl.KeyStore;
import io.gravitee.plugin.annotation.processor.result.ssl.KeyStoreType;
import java.io.Serial;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class NoneKeyStore extends KeyStore {

    @Serial
    private static final long serialVersionUID = -2540354913966457704L;

    public NoneKeyStore() {
        super(KeyStoreType.NONE);
    }

    public static NoneKeyStoreBuilder builder() {
        return new NoneKeyStoreBuilder();
    }

    public static class NoneKeyStoreBuilder {

        NoneKeyStoreBuilder() {}

        public NoneKeyStore build() {
            return new NoneKeyStore();
        }

        public String toString() {
            return "NoneKeyStore.NoneKeyStoreBuilder()";
        }
    }
}
