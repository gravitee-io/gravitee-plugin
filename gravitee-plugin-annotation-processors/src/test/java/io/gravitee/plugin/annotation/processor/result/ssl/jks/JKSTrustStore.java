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
package io.gravitee.plugin.annotation.processor.result.ssl.jks;

import io.gravitee.plugin.annotation.processor.result.ssl.TrustStore;
import io.gravitee.plugin.annotation.processor.result.ssl.TrustStoreType;
import io.gravitee.secrets.api.annotation.Secret;
import io.gravitee.secrets.api.el.FieldKind;
import java.io.Serial;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class JKSTrustStore extends TrustStore {

    @Serial
    private static final long serialVersionUID = -6603840868190194763L;

    private String path;
    private String content;

    @Secret(FieldKind.PASSWORD)
    private String password;

    private String alias;

    public JKSTrustStore() {
        super(TrustStoreType.JKS);
    }

    public JKSTrustStore(String path, String content, String password, String alias) {
        super(TrustStoreType.JKS);
        this.path = path;
        this.content = content;
        this.password = password;
        this.alias = alias;
    }

    public static JKSTrustStoreBuilder builder() {
        return new JKSTrustStoreBuilder();
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

    public static class JKSTrustStoreBuilder {

        private String path;
        private String content;
        private String password;
        private String alias;

        JKSTrustStoreBuilder() {}

        public JKSTrustStoreBuilder path(String path) {
            this.path = path;
            return this;
        }

        public JKSTrustStoreBuilder content(String content) {
            this.content = content;
            return this;
        }

        public JKSTrustStoreBuilder password(String password) {
            this.password = password;
            return this;
        }

        public JKSTrustStoreBuilder alias(String alias) {
            this.alias = alias;
            return this;
        }

        public JKSTrustStore build() {
            return new JKSTrustStore(this.path, this.content, this.password, this.alias);
        }

        public String toString() {
            return (
                "JKSTrustStore.JKSTrustStoreBuilder(path=" +
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
