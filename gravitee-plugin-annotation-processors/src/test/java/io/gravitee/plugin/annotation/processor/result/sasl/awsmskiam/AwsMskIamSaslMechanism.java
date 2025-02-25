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
package io.gravitee.plugin.annotation.processor.result.sasl.awsmskiam;

import io.gravitee.plugin.annotation.processor.result.sasl.SaslMechanism;
import io.gravitee.plugin.annotation.processor.result.sasl.SaslMechanismType;

/**
 * @author Remi Baptiste (remi.baptiste at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AwsMskIamSaslMechanism extends SaslMechanism {

    public AwsMskIamSaslMechanism() {
        super(SaslMechanismType.AWS_MSK_IAM);
    }

    private AwsMskIamSaslMechanismConfig config;

    public AwsMskIamSaslMechanismConfig getConfig() {
        return this.config;
    }

    public void setConfig(AwsMskIamSaslMechanismConfig config) {
        this.config = config;
    }

    public static class AwsMskIamSaslMechanismConfig {

        private String saslJaasConfig;

        public String getSaslJaasConfig() {
            return this.saslJaasConfig;
        }

        public void setSaslJaasConfig(String saslJaasConfig) {
            this.saslJaasConfig = saslJaasConfig;
        }
    }
}
