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
package io.gravitee.plugin.annotation.processor.result;

public enum SecurityProtocol {
    /**
     * No authentication no encryption
     */
    PLAINTEXT("PLAINTEXT"),
    /**
     * SASL authentication with no TLS
     */
    SASL_PLAINTEXT("SASL_PLAINTEXT"),
    /**
     * SASL authentication with TLS/SSL encryption
     */
    SASL_SSL("SASL_SSL"),
    /**
     * SSL for both encryption and authentication without SASL
     */
    SSL("SSL");

    private SecurityProtocol(String value) {
        this.value = value;
    }

    private final String value;
}
