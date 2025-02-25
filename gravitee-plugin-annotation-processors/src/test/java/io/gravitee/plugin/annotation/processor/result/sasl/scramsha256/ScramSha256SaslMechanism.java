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
package io.gravitee.plugin.annotation.processor.result.sasl.scramsha256;

import io.gravitee.plugin.annotation.processor.result.sasl.SaslMechanism;
import io.gravitee.plugin.annotation.processor.result.sasl.SaslMechanismType;
import io.gravitee.secrets.api.annotation.Secret;
import io.gravitee.secrets.api.el.FieldKind;
import java.util.Map;

/**
 * @author Remi Baptiste (remi.baptiste at graviteesource.com)
 * @author GraviteeSource Team
 */
public class ScramSha256SaslMechanism extends SaslMechanism {

    public ScramSha256SaslMechanism() {
        super(SaslMechanismType.SCRAM_SHA_256);
    }

    @Secret
    private String username;

    @Secret(FieldKind.PASSWORD)
    private String password;

    @Secret
    private Map<String, String> customJassConfigMap;

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public Map<String, String> getCustomJassConfigMap() {
        return this.customJassConfigMap;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCustomJassConfigMap(Map<String, String> customJassConfigMap) {
        this.customJassConfigMap = customJassConfigMap;
    }
}
