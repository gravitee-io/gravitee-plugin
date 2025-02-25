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
package io.gravitee.plugin.annotation.processor.result.sasl;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Remi Baptiste (remi.baptiste at graviteesource.com)
 * @author GraviteeSource Team
 */
public enum SaslMechanismType {
    /**
     * No authentication is required.
     */
    NONE("NONE"),
    /**
     * The AWS_MSK_IAM mechanism enables the use of AWS IAM credentials for authentication.
     */
    AWS_MSK_IAM("AWS_MSK_IAM"),

    /**
     * SASL/SCRAM is a family of SASL mechanisms that addresses the security concerns with traditional mechanisms that perform username/password authentication like PLAIN.
     */
    SCRAM_SHA_256("SCRAM-SHA-256");

    @JsonValue
    private final String value;

    private SaslMechanismType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
