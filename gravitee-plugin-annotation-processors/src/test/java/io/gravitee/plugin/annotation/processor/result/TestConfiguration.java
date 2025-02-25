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

import io.gravitee.common.http.HttpHeader;
import io.gravitee.plugin.annotation.ConfigurationEvaluator;
import io.gravitee.plugin.annotation.processor.result.sasl.SaslMechanism;
import io.gravitee.plugin.annotation.processor.result.sasl.SaslMechanismType;
import io.gravitee.plugin.annotation.processor.result.sasl.none.NoneSaslMechanism;
import io.gravitee.plugin.annotation.processor.result.ssl.SslOptions;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ConfigurationEvaluator(attributePrefix = "gravitee.attributes.endpoint.test")
public class TestConfiguration {

    private static final String CONSTANT = "my_constant_should_be_ignored";

    //Enum
    private SecurityProtocol protocol = SecurityProtocol.PLAINTEXT;

    //Object
    private Ssl ssl;

    //With Jackson annotation
    private SslOptions sslOptions;

    private SaslMechanism saslMechanism;

    @Valid
    private SecurityConfiguration security;

    //Inner class
    @Valid
    private Consumer consumer = new Consumer();

    //Inner class with name different from class name
    @Valid
    private Authentication auth;

    private List<HttpHeader> headers = new ArrayList<>();

    public TestConfiguration(
        SecurityProtocol protocol,
        Ssl ssl,
        SaslMechanism saslMechanism,
        SecurityConfiguration security,
        Consumer consumer,
        Authentication auth,
        List<HttpHeader> headers
    ) {
        this.protocol = protocol;
        this.ssl = ssl;
        this.saslMechanism = saslMechanism;
        this.security = security;
        this.consumer = consumer;
        this.auth = auth;
        this.headers = headers;
    }

    public TestConfiguration() {}

    public boolean isSslEnabled() {
        return (ssl != null && ((ssl.getKeyStore() != null && ssl.getKeyStore().getKey() != null)));
    }

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

    public SaslMechanism getSaslMechanism() {
        return saslMechanism;
    }

    public void setSaslMechanism(SaslMechanism saslMechanism) {
        this.saslMechanism = saslMechanism;
    }

    public Authentication getAuth() {
        return auth;
    }

    public void setAuth(Authentication auth) {
        this.auth = auth;
    }

    public List<HttpHeader> getHeaders() {
        return headers;
    }

    public void setHeaders(List<HttpHeader> headers) {
        this.headers = headers;
    }

    public SslOptions getSslOptions() {
        return sslOptions;
    }

    public void setSslOptions(SslOptions sslOptions) {
        this.sslOptions = sslOptions;
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

    private static SaslMechanism $default$saslMechanism() {
        return new NoneSaslMechanism();
    }

    private static Authentication $default$auth() {
        return new Authentication();
    }

    private static List<HttpHeader> $default$headers() {
        return new ArrayList<>();
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

        private Integer windowSize;

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

        public Integer getWindowSize() {
            return windowSize;
        }

        public void setWindowSize(final Integer windowSize) {
            this.windowSize = windowSize;
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

    public static class Authentication {

        private String username;
        private String password;

        public Authentication(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public Authentication() {}

        public static AuthenticationBuilder builder() {
            return new AuthenticationBuilder();
        }

        public String getUsername() {
            return this.username;
        }

        public String getPassword() {
            return this.password;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof Authentication)) return false;
            final Authentication other = (Authentication) o;
            if (!other.canEqual((Object) this)) return false;
            final Object this$username = this.getUsername();
            final Object other$username = other.getUsername();
            if (this$username == null ? other$username != null : !this$username.equals(other$username)) return false;
            final Object this$password = this.getPassword();
            final Object other$password = other.getPassword();
            if (this$password == null ? other$password != null : !this$password.equals(other$password)) return false;
            return true;
        }

        protected boolean canEqual(final Object other) {
            return other instanceof Authentication;
        }

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $username = this.getUsername();
            result = result * PRIME + ($username == null ? 43 : $username.hashCode());
            final Object $password = this.getPassword();
            result = result * PRIME + ($password == null ? 43 : $password.hashCode());
            return result;
        }

        public static class AuthenticationBuilder {

            private String username;
            private String password;

            AuthenticationBuilder() {}

            public AuthenticationBuilder username(String username) {
                this.username = username;
                return this;
            }

            public AuthenticationBuilder password(String password) {
                this.password = password;
                return this;
            }

            public Authentication build() {
                return new Authentication(this.username, this.password);
            }

            public String toString() {
                return (
                    "TestConfiguration.Authentication.AuthenticationBuilder(username=" + this.username + ", password=" + this.password + ")"
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

        private SaslMechanismType type = SaslMechanismType.NONE;
        private SaslMechanism saslMechanism$value;
        private boolean saslMechanism$set;

        private Authentication auth$value;
        private boolean auth$set;

        private List<HttpHeader> headers$value;
        private boolean headers$set;

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

        public TestConfigurationBuilder saslMechanism(SaslMechanism saslMechanism) {
            this.saslMechanism$value = saslMechanism;
            this.saslMechanism$set = true;
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

        public TestConfigurationBuilder auth(Authentication auth) {
            this.auth$value = auth;
            this.auth$set = true;
            return this;
        }

        public TestConfigurationBuilder headers(List<HttpHeader> headers) {
            this.headers$value = headers;
            this.headers$set = true;
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

            SaslMechanism saslMechanism$value = this.saslMechanism$value;
            if (!this.saslMechanism$set) {
                saslMechanism$value = TestConfiguration.$default$saslMechanism();
            }

            Authentication auth$value = this.auth$value;
            if (!this.auth$set) {
                auth$value = TestConfiguration.$default$auth();
            }

            List<HttpHeader> headers$value = this.headers$value;
            if (!this.headers$set) {
                headers$value = TestConfiguration.$default$headers();
            }
            return new TestConfiguration(
                protocol,
                ssl$value,
                saslMechanism$value,
                security$value,
                consumer$value,
                auth$value,
                headers$value
            );
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
