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

import io.gravitee.plugin.annotation.processor.result.ssl.TrustStore;
import io.gravitee.plugin.annotation.processor.result.ssl.TrustStoreType;
import io.gravitee.secrets.api.annotation.Secret;
import io.gravitee.secrets.api.el.FieldKind;
import java.io.Serial;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class PKCS12TrustStore extends TrustStore {

    @Serial
    private static final long serialVersionUID = 3915578060196536545L;

    private String path;
    private String content;

    @Secret(FieldKind.PASSWORD)
    private String password;

    private String alias;

    public PKCS12TrustStore() {
        super(TrustStoreType.PKCS12);
    }

    public PKCS12TrustStore(String path, String content, String password, String alias) {
        super(TrustStoreType.PKCS12);
        this.path = path;
        this.content = content;
        this.password = password;
        this.alias = alias;
    }

    public static PKCS12TrustStoreBuilder builder() {
        return new PKCS12TrustStoreBuilder();
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

    public static class PKCS12TrustStoreBuilder {

        private String path;
        private String content;
        private String password;
        private String alias;

        PKCS12TrustStoreBuilder() {}

        public PKCS12TrustStoreBuilder path(String path) {
            this.path = path;
            return this;
        }

        public PKCS12TrustStoreBuilder content(String content) {
            this.content = content;
            return this;
        }

        public PKCS12TrustStoreBuilder password(String password) {
            this.password = password;
            return this;
        }

        public PKCS12TrustStoreBuilder alias(String alias) {
            this.alias = alias;
            return this;
        }

        public PKCS12TrustStore build() {
            return new PKCS12TrustStore(this.path, this.content, this.password, this.alias);
        }

        public String toString() {
            return (
                "PKCS12TrustStore.PKCS12TrustStoreBuilder(path=" +
                this.path +
                ", content=" +
                this.content +
                ", password=" +
                this.password +
                ", alias=" +
                this.alias +
                ")"
            );
        }
    }
}
