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
/**
 * @author Remi Baptiste (remi.baptiste at graviteesource.com)
 * @author GraviteeSource Team
 */
package io.gravitee.plugin.annotation.processor.result;

import io.gravitee.plugin.annotation.ConfigurationEvaluator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;

@ConfigurationEvaluator(attributePrefix = "gravitee.attributes.endpoint.test")
public class TestConfiguration {

    private static final String CONSTANT = "my_constant_should_be_ignored";

    //Enum
    private SecurityProtocol protocol = SecurityProtocol.PLAINTEXT;

    //Object
    private Ssl ssl;

    @Valid
    private SecurityConfiguration security;

    //Inner class
    @Valid
    private Consumer consumer = new Consumer();

    public TestConfiguration(SecurityProtocol protocol, Ssl ssl, SecurityConfiguration security, Consumer consumer) {
        this.protocol = protocol;
        this.ssl = ssl;
        this.security = security;
        this.consumer = consumer;
    }

    public TestConfiguration() {}

    public SecurityProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(SecurityProtocol protocol) {
        this.protocol = protocol;
    }

    public Ssl getSsl() {
        return ssl;
    }

    public void setSsl(Ssl ssl) {
        this.ssl = ssl;
    }

    public TestConfiguration.Consumer getConsumer() {
        return consumer;
    }

    public void setConsumer(TestConfiguration.Consumer consumer) {
        this.consumer = consumer;
    }

    public SecurityConfiguration getSecurity() {
        return this.security;
    }

    public void setSecurity(SecurityConfiguration security) {
        this.security = security;
    }

    private static TestConfiguration.Consumer $default$consumer() {
        return new Consumer();
    }

    private static SecurityConfiguration $default$security() {
        return new SecurityConfiguration();
    }

    private static Ssl $default$ssl() {
        return new Ssl();
    }

    public static TestConfigurationBuilder builder() {
        return new TestConfigurationBuilder();
    }

    public static class Consumer {

        private boolean enabled;

        @Pattern(regexp = "latest|earliest|none")
        private String autoOffsetReset = "latest";

        @Size(min = 1)
        private Set<String> topics;

        //Added to validate proper behavior when a String attribute is null
        private String topicPattern;

        private TrustStore trustStore;

        private List<String> attributes;

        public Consumer(boolean enabled, Set<String> topics, String autoOffsetReset, List<String> attributes) {
            this.enabled = enabled;
            this.topics = topics;
            this.autoOffsetReset = autoOffsetReset;
            this.attributes = attributes;
        }

        public Consumer() {}

        public boolean isEnabled() {
            return this.enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getAutoOffsetReset() {
            return this.autoOffsetReset;
        }

        public void setAutoOffsetReset(String autoOffsetReset) {
            this.autoOffsetReset = autoOffsetReset;
        }

        public Set<String> getTopics() {
            return this.topics;
        }

        public void setTopics(Set<String> topics) {
            this.topics = topics;
        }

        public String getTopicPattern() {
            return topicPattern;
        }

        public void setTopicPattern(String topicPattern) {
            this.topicPattern = topicPattern;
        }

        public TrustStore getTrustStore() {
            return trustStore;
        }

        public void setTrustStore(TrustStore trustStore) {
            this.trustStore = trustStore;
        }

        public void setAttributes(List<String> attributes) {
            this.attributes = attributes;
        }

        public List<String> getAttributes() {
            return attributes;
        }

        public static ConsumerBuilder builder() {
            return new ConsumerBuilder();
        }

        @Pattern(regexp = "latest|earliest|none")
        private static String $default$autoOffsetReset() {
            return "latest";
        }

        public static class ConsumerBuilder {

            private boolean enabled;

            @Size(min = 1)
            private Set<String> topics;

            @Pattern(regexp = "latest|earliest|none")
            private String autoOffsetReset$value;

            private boolean autoOffsetReset$set;

            private List<String> attributes;

            ConsumerBuilder() {}

            public ConsumerBuilder enabled(boolean enabled) {
                this.enabled = enabled;
                return this;
            }

            public ConsumerBuilder topics(@Size(min = 1) Set<String> topics) {
                this.topics = topics;
                return this;
            }

            public ConsumerBuilder attributes(List<String> attributes) {
                this.attributes = attributes;
                return this;
            }

            public ConsumerBuilder autoOffsetReset(@Pattern(regexp = "latest|earliest|none") String autoOffsetReset) {
                this.autoOffsetReset$value = autoOffsetReset;
                this.autoOffsetReset$set = true;
                return this;
            }

            public Consumer build() {
                String autoOffsetReset$value = this.autoOffsetReset$value;
                if (!this.autoOffsetReset$set) {
                    autoOffsetReset$value = TestConfiguration.Consumer.$default$autoOffsetReset();
                }
                return new Consumer(this.enabled, this.topics, autoOffsetReset$value, this.attributes);
            }

            public String toString() {
                return (
                    "ExampleConfiguration.Consumer.ConsumerBuilder(enabled=" +
                    this.enabled +
                    ", topics=" +
                    this.topics +
                    ", autoOffsetReset$value=" +
                    this.autoOffsetReset$value +
                    ", attributes=" +
                    this.attributes +
                    ")"
                );
            }
        }
    }

    public static class TestConfigurationBuilder {

        private Consumer consumer$value;
        private boolean consumer$set;
        private SecurityConfiguration security$value;
        private boolean security$set;

        private SecurityProtocol protocol = SecurityProtocol.PLAINTEXT;
        private Ssl ssl$value;
        private boolean ssl$set;

        TestConfigurationBuilder() {}

        public TestConfigurationBuilder consumer(Consumer consumer) {
            this.consumer$value = consumer;
            this.consumer$set = true;
            return this;
        }

        public TestConfigurationBuilder ssl(Ssl ssl) {
            this.ssl$value = ssl;
            this.ssl$set = true;
            return this;
        }

        public TestConfigurationBuilder protocol(SecurityProtocol protocol) {
            this.protocol = protocol;
            return this;
        }

        public TestConfigurationBuilder security(SecurityConfiguration security) {
            this.security$value = security;
            this.security$set = true;
            return this;
        }

        public TestConfiguration build() {
            TestConfiguration.Consumer consumer$value = this.consumer$value;
            if (!this.consumer$set) {
                consumer$value = TestConfiguration.$default$consumer();
            }
            SecurityConfiguration security$value = this.security$value;
            if (!this.security$set) {
                security$value = TestConfiguration.$default$security();
            }

            Ssl ssl$value = this.ssl$value;
            if (!this.ssl$set) {
                ssl$value = TestConfiguration.$default$ssl();
            }
            return new TestConfiguration(protocol, ssl$value, security$value, consumer$value);
        }

        public String toString() {
            return (
                "ExampleConfiguration.TestConfigurationBuilder(consumer$value=" +
                this.consumer$value +
                ", security$value=" +
                this.security$value +
                ")"
            );
        }
    }
}
