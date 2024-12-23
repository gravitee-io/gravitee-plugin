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

import io.gravitee.plugin.annotation.processor.result.ssl.TrustStore;
import io.gravitee.plugin.annotation.processor.result.ssl.TrustStoreType;
import java.io.Serial;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class PEMTrustStore extends TrustStore {

    @Serial
    private static final long serialVersionUID = 7432939542056493096L;

    private String path;
    private String content;

    public PEMTrustStore() {
        super(TrustStoreType.PEM);
    }

    public PEMTrustStore(String path, String content) {
        super(TrustStoreType.PEM);
        this.path = path;
        this.content = content;
    }

    public static PEMTrustStoreBuilder builder() {
        return new PEMTrustStoreBuilder();
    }

    public String getPath() {
        return this.path;
    }

    public String getContent() {
        return this.content;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public static class PEMTrustStoreBuilder {

        private String path;
        private String content;

        PEMTrustStoreBuilder() {}

        public PEMTrustStoreBuilder path(String path) {
            this.path = path;
            return this;
        }

        public PEMTrustStoreBuilder content(String content) {
            this.content = content;
            return this;
        }

        public PEMTrustStore build() {
            return new PEMTrustStore(this.path, this.content);
        }

        public String toString() {
            return "PEMTrustStore.PEMTrustStoreBuilder(path=" + this.path + ", content=" + this.content + ")";
        }
    }
}
