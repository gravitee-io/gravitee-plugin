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
package io.gravitee.plugin.annotation.processor.result.ssl;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSKeyStore;
import io.gravitee.plugin.annotation.processor.result.ssl.none.NoneKeyStore;
import io.gravitee.plugin.annotation.processor.result.ssl.pem.PEMKeyStore;
import io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12KeyStore;
import java.io.Serial;
import java.io.Serializable;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", defaultImpl = NoneKeyStore.class)
@JsonSubTypes(
    {
        @JsonSubTypes.Type(names = { "JKS", "jks" }, value = JKSKeyStore.class),
        @JsonSubTypes.Type(names = { "PEM", "pem" }, value = PEMKeyStore.class),
        @JsonSubTypes.Type(names = { "PKCS12", "pkcs12" }, value = PKCS12KeyStore.class),
        @JsonSubTypes.Type(names = { "NONE", "none" }, value = NoneKeyStore.class),
    }
)
public abstract class KeyStore implements Serializable {

    @Serial
    private static final long serialVersionUID = -917896495926741784L;

    private final KeyStoreType type;

    protected KeyStore(KeyStoreType type) {
        this.type = type;
    }

    public KeyStoreType getType() {
        return this.type;
    }
}
