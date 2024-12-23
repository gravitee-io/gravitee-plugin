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

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class SslOptions implements Serializable {

    @Serial
    private static final long serialVersionUID = 5578794192878572915L;

    private boolean trustAll;

    private boolean hostnameVerifier = true;

    private TrustStore trustStore;

    private KeyStore keyStore;

    public SslOptions(boolean trustAll, boolean hostnameVerifier, TrustStore trustStore, KeyStore keyStore) {
        this.trustAll = trustAll;
        this.hostnameVerifier = hostnameVerifier;
        this.trustStore = trustStore;
        this.keyStore = keyStore;
    }

    public SslOptions() {}

    private static boolean $default$hostnameVerifier() {
        return true;
    }

    public static SslOptionsBuilder builder() {
        return new SslOptionsBuilder();
    }

    public boolean isTrustAll() {
        return this.trustAll;
    }

    public boolean isHostnameVerifier() {
        return this.hostnameVerifier;
    }

    public TrustStore getTrustStore() {
        return this.trustStore;
    }

    public KeyStore getKeyStore() {
        return this.keyStore;
    }

    public void setTrustAll(boolean trustAll) {
        this.trustAll = trustAll;
    }

    public void setHostnameVerifier(boolean hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
    }

    public void setTrustStore(TrustStore trustStore) {
        this.trustStore = trustStore;
    }

    public void setKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    public static class SslOptionsBuilder {

        private boolean trustAll;
        private boolean hostnameVerifier$value;
        private boolean hostnameVerifier$set;
        private TrustStore trustStore;
        private KeyStore keyStore;

        SslOptionsBuilder() {}

        public SslOptionsBuilder trustAll(boolean trustAll) {
            this.trustAll = trustAll;
            return this;
        }

        public SslOptionsBuilder hostnameVerifier(boolean hostnameVerifier) {
            this.hostnameVerifier$value = hostnameVerifier;
            this.hostnameVerifier$set = true;
            return this;
        }

        public SslOptionsBuilder trustStore(TrustStore trustStore) {
            this.trustStore = trustStore;
            return this;
        }

        public SslOptionsBuilder keyStore(KeyStore keyStore) {
            this.keyStore = keyStore;
            return this;
        }

        public SslOptions build() {
            boolean hostnameVerifier$value = this.hostnameVerifier$value;
            if (!this.hostnameVerifier$set) {
                hostnameVerifier$value = SslOptions.$default$hostnameVerifier();
            }
            return new SslOptions(this.trustAll, hostnameVerifier$value, this.trustStore, this.keyStore);
        }

        public String toString() {
            return (
                "SslOptions.SslOptionsBuilder(trustAll=" +
                this.trustAll +
                ", hostnameVerifier$value=" +
                this.hostnameVerifier$value +
                ", trustStore=" +
                this.trustStore +
                ", keyStore=" +
                this.keyStore +
                ")"
            );
        }
    }
}
