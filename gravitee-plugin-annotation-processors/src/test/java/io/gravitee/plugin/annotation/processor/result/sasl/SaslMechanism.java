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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.gravitee.plugin.annotation.processor.result.sasl.awsmskiam.AwsMskIamSaslMechanism;
import io.gravitee.plugin.annotation.processor.result.sasl.none.NoneSaslMechanism;
import io.gravitee.plugin.annotation.processor.result.sasl.scramsha256.ScramSha256SaslMechanism;

/**
 * @author Remi Baptiste (remi.baptiste at graviteesource.com)
 * @author GraviteeSource Team
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes(
    {
        @JsonSubTypes.Type(names = { "NONE" }, value = NoneSaslMechanism.class),
        @JsonSubTypes.Type(names = { "AWS_MSK_IAM" }, value = AwsMskIamSaslMechanism.class),
        @JsonSubTypes.Type(names = { "SCRAM-SHA-256" }, value = ScramSha256SaslMechanism.class),
    }
)
public abstract class SaslMechanism {

    protected final SaslMechanismType type;

    protected SaslMechanism(SaslMechanismType type) {
        this.type = type;
    }

    public SaslMechanismType getType() {
        return this.type;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof SaslMechanism)) return false;
        final SaslMechanism other = (SaslMechanism) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$type = this.getType();
        final Object other$type = other.getType();
        if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof SaslMechanism;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $type = this.getType();
        result = result * PRIME + ($type == null ? 43 : $type.hashCode());
        return result;
    }

    public String toString() {
        return "SaslMechanism(type=" + this.getType() + ")";
    }
}
