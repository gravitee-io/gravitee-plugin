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
package io.gravitee.plugin.annotation.processor.result.ssl.pkcs12;

import io.gravitee.plugin.annotation.processor.result.ssl.KeyStore;
import io.gravitee.plugin.annotation.processor.result.ssl.KeyStoreType;
import io.gravitee.secrets.api.annotation.Secret;
import io.gravitee.secrets.api.el.FieldKind;
import java.io.Serial;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class PKCS12KeyStore extends KeyStore {

    @Serial
    private static final long serialVersionUID = 1210626721233767960L;

    private String path;
    private String content;

    @Secret(FieldKind.PASSWORD)
    private String password;

    private String alias;

    /**
     * For PKCS12, if specified, it must be equals to password as the JDK doesn't support a key password different from the keystore password.
     */
    private String keyPassword;

    public PKCS12KeyStore() {
        super(KeyStoreType.PKCS12);
    }

    public PKCS12KeyStore(String path, String content, String password, String alias, String keyPassword) {
        super(KeyStoreType.PKCS12);
        this.path = path;
        this.content = content;
        this.password = password;
        this.alias = alias;
        this.keyPassword = keyPassword;
    }

    public static PKCS12KeyStoreBuilder builder() {
        return new PKCS12KeyStoreBuilder();
    }

    public String getPath() {
        return this.path;
    }

    public String getContent() {
        return this.content;
    }

    public String getPassword() {
        return this.password;
    }

    public String getAlias() {
        return this.alias;
    }

    public String getKeyPassword() {
        return this.keyPassword;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public static class PKCS12KeyStoreBuilder {

        private String path;
        private String content;
        private String password;
        private String alias;
        private String keyPassword;

        PKCS12KeyStoreBuilder() {}

        public PKCS12KeyStoreBuilder path(String path) {
            this.path = path;
            return this;
        }

        public PKCS12KeyStoreBuilder content(String content) {
            this.content = content;
            return this;
        }

        public PKCS12KeyStoreBuilder password(String password) {
            this.password = password;
            return this;
        }

        public PKCS12KeyStoreBuilder alias(String alias) {
            this.alias = alias;
            return this;
        }

        public PKCS12KeyStoreBuilder keyPassword(String keyPassword) {
            this.keyPassword = keyPassword;
            return this;
        }

        public PKCS12KeyStore build() {
            return new PKCS12KeyStore(this.path, this.content, this.password, this.alias, this.keyPassword);
        }

        public String toString() {
            return (
                "PKCS12KeyStore.PKCS12KeyStoreBuilder(path=" +
                this.path +
                ", content=" +
                this.content +
                ", password=" +
                this.password +
                ", alias=" +
                this.alias +
                ", keyPassword=" +
                this.keyPassword +
                ")"
            );
        }
    }
}