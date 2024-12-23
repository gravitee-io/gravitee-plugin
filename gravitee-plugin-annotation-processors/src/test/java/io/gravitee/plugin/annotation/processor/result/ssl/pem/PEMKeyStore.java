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
package io.gravitee.plugin.annotation.processor.result.ssl.pem;

import io.gravitee.plugin.annotation.processor.result.ssl.KeyStore;
import io.gravitee.plugin.annotation.processor.result.ssl.KeyStoreType;
import java.io.Serial;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class PEMKeyStore extends KeyStore {

    @Serial
    private static final long serialVersionUID = 1051430527272519608L;

    private String keyPath;
    private String keyContent;
    private String certPath;
    private String certContent;

    public PEMKeyStore() {
        super(KeyStoreType.PEM);
    }

    public PEMKeyStore(String keyPath, String keyContent, String certPath, String certContent) {
        super(KeyStoreType.PEM);
        this.keyPath = keyPath;
        this.keyContent = keyContent;
        this.certPath = certPath;
        this.certContent = certContent;
    }

    public static PEMKeyStoreBuilder builder() {
        return new PEMKeyStoreBuilder();
    }

    public String getKeyPath() {
        return this.keyPath;
    }

    public String getKeyContent() {
        return this.keyContent;
    }

    public String getCertPath() {
        return this.certPath;
    }

    public String getCertContent() {
        return this.certContent;
    }

    public void setKeyPath(String keyPath) {
        this.keyPath = keyPath;
    }

    public void setKeyContent(String keyContent) {
        this.keyContent = keyContent;
    }

    public void setCertPath(String certPath) {
        this.certPath = certPath;
    }

    public void setCertContent(String certContent) {
        this.certContent = certContent;
    }

    public static class PEMKeyStoreBuilder {

        private String keyPath;
        private String keyContent;
        private String certPath;
        private String certContent;

        PEMKeyStoreBuilder() {}

        public PEMKeyStoreBuilder keyPath(String keyPath) {
            this.keyPath = keyPath;
            return this;
        }

        public PEMKeyStoreBuilder keyContent(String keyContent) {
            this.keyContent = keyContent;
            return this;
        }

        public PEMKeyStoreBuilder certPath(String certPath) {
            this.certPath = certPath;
            return this;
        }

        public PEMKeyStoreBuilder certContent(String certContent) {
            this.certContent = certContent;
            return this;
        }

        public PEMKeyStore build() {
            return new PEMKeyStore(this.keyPath, this.keyContent, this.certPath, this.certContent);
        }

        public String toString() {
            return (
                "PEMKeyStore.PEMKeyStoreBuilder(keyPath=" +
                this.keyPath +
                ", keyContent=" +
                this.keyContent +
                ", certPath=" +
                this.certPath +
                ", certContent=" +
                this.certContent +
                ")"
            );
        }
    }
}
