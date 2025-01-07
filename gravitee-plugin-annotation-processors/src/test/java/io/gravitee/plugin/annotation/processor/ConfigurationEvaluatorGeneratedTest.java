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
package io.gravitee.plugin.annotation.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import io.gravitee.common.http.HttpHeader;
import io.gravitee.el.TemplateContext;
import io.gravitee.el.TemplateEngine;
import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.reactive.api.ExecutionFailure;
import io.gravitee.gateway.reactive.api.context.InternalContextAttributes;
import io.gravitee.gateway.reactive.api.context.TlsSession;
import io.gravitee.gateway.reactive.api.context.http.HttpPlainExecutionContext;
import io.gravitee.gateway.reactive.api.context.http.HttpPlainRequest;
import io.gravitee.gateway.reactive.api.context.http.HttpPlainResponse;
import io.gravitee.gateway.reactive.api.tracing.Tracer;
import io.gravitee.plugin.annotation.processor.result.*;
import io.gravitee.reporter.api.v4.metric.Metrics;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import java.util.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Remi Baptiste (remi.baptiste at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationEvaluatorGeneratedTest {

    TestConfigurationEvaluator evaluator;

    @Mock
    TemplateEngine templateEngine;

    @Mock
    TemplateContext templateContext;

    @Before
    public void before() {
        TestConfiguration configuration = new TestConfiguration();
        configuration.getConsumer().setEnabled(true);
        configuration.getConsumer().setAutoOffsetReset("none");
        configuration.getConsumer().setTrustStore(new TrustStore());
        configuration.getConsumer().getTrustStore().setKey("{#secrets.get('/vault/secret/test:password')}");
        configuration.getConsumer().setAttributes(List.of("attribute1", "{#dictionaries['my-dictionary']['attribute2']}"));
        configuration.setHeaders(List.of(new HttpHeader("token", "{#secrets.get('/vault/secret/test:token')}")));
        evaluator = new TestConfigurationEvaluator(configuration);
    }

    @Test
    public void should_interrupt_with_error_on_validation() {
        when(templateEngine.eval("none", String.class)).thenReturn(Maybe.just("result"));
        when(templateEngine.eval("{#secrets.get('/vault/secret/test:password')}", String.class)).thenReturn(Maybe.just("password"));
        when(templateEngine.getTemplateContext()).thenReturn(templateContext);
        var context = new DefaultExecutionContext(templateEngine);

        TestObserver<TestConfiguration> testObserver = evaluator.eval(context).test();

        testObserver.assertError(throwable -> {
            assertThat(throwable).isInstanceOf(IllegalStateException.class);
            assertThat(throwable.getMessage()).isEqualTo("Invalid configuration");
            return true;
        });
    }

    @Test
    public void should_return_evaluated_configuration() {
        var spiedTemplateEngine = spy(new DefaultTemplateEngine());
        when(spiedTemplateEngine.eval("none", String.class)).thenReturn(Maybe.just("latest"));
        when(spiedTemplateEngine.eval("{#dictionaries['my-dictionary']['attribute2']}", String.class))
            .thenReturn(Maybe.just("my_dictionary_attribute"));
        when(spiedTemplateEngine.eval("{#secrets.get('/vault/secret/test:password')}", String.class)).thenReturn(Maybe.just("password"));
        when(spiedTemplateEngine.eval("{#secrets.get('/vault/secret/test:token')}", String.class))
            .thenReturn(Maybe.just("my_secret_token"));
        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("gravitee.attributes.endpoint.test.protocol", "SSL");
        contextMap.put("gravitee.attributes.endpoint.test.consumer.topics", "topic1,topic2");
        var context = new DefaultExecutionContext(spiedTemplateEngine, contextMap);

        evaluator
            .eval(context)
            .test()
            .assertComplete()
            .assertValue(testConfiguration -> {
                assertThat(testConfiguration.getConsumer().getAutoOffsetReset()).isEqualTo("latest");
                assertThat(testConfiguration.getConsumer().getTopics()).isEqualTo(Set.of("topic1", "topic2"));
                assertThat(testConfiguration.getConsumer().getAttributes()).isNotEmpty();
                assertThat(testConfiguration.getConsumer().getAttributes()).containsExactly("attribute1", "my_dictionary_attribute");
                assertThat(testConfiguration.getHeaders()).isNotEmpty();
                assertThat(testConfiguration.getHeaders().get(0).getValue()).isEqualTo("my_secret_token");
                assertThat(testConfiguration.getProtocol()).isEqualTo(SecurityProtocol.SSL);
                return true;
            });
    }

    @Test
    public void should_cache_evaluated_configuration() {
        when(templateEngine.eval("none", String.class)).thenReturn(Maybe.just("latest"));
        when(templateEngine.eval("attribute1", String.class)).thenReturn(Maybe.just("attribute1"));
        when(templateEngine.eval("{#dictionaries['my-dictionary']['attribute2']}", String.class))
            .thenReturn(Maybe.just("my_dictionary_attribute"));
        when(templateEngine.eval("{#secrets.get('/vault/secret/test:password')}", String.class)).thenReturn(Maybe.just("password"));
        when(templateEngine.eval("{#secrets.get('/vault/secret/test:token')}", String.class)).thenReturn(Maybe.just("my_secret_token"));
        when(templateEngine.getTemplateContext()).thenReturn(templateContext);
        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("gravitee.attributes.endpoint.test.protocol", "SSL");
        contextMap.put("gravitee.attributes.endpoint.test.consumer.topics", "topic1,topic2");
        var context = new DefaultExecutionContext(templateEngine, contextMap);
        evaluator
            .eval(context)
            .test()
            .assertComplete()
            .assertValue(testConfiguration -> {
                assertThat(testConfiguration.getConsumer().getAutoOffsetReset()).isEqualTo("latest");
                assertThat(testConfiguration.getProtocol()).isEqualTo(SecurityProtocol.SSL);
                assertThat(testConfiguration.getConsumer().getTopics()).isEqualTo(Set.of("topic1", "topic2"));

                assertThat(context.getInternalAttributes().values()).hasSize(1).containsExactly(testConfiguration);
                return true;
            });
    }

    @Test
    public void should_return_evaluated_configuration_from_internal_attribute() {
        var spiedContext = spy(new DefaultExecutionContext());
        TestConfiguration expectedConfiguration = new TestConfiguration();
        when(spiedContext.getInternalAttribute(anyString())).thenReturn(expectedConfiguration);

        evaluator
            .eval(spiedContext)
            .test()
            .assertComplete()
            .assertValue(testConfiguration -> {
                assertThat(testConfiguration).isSameAs(expectedConfiguration);
                return true;
            });

        verify(spiedContext, times(0)).setInternalAttribute(anyString(), any(TestConfiguration.class));
    }

    @Test
    public void should_return_original_configuration() {
        var consumer = TestConfiguration.Consumer
            .builder()
            .enabled(true)
            .autoOffsetReset("none")
            .attributes(List.of("attribute1"))
            .topics(Set.of("topic1"))
            .build();
        consumer.setTopicPattern("topic-pattern");
        consumer.setTrustStore(TrustStore.builder().key("my-key").build());
        var securityConfiguration = SecurityConfiguration.builder().property("my-prop").build();
        var ssl = Ssl.builder().keyStore(KeyStore.builder().key("keystore-key").build()).timeout(10L).build();
        var auth = TestConfiguration.Authentication.builder().username("username").password("password").build();
        var headers = List.of(new HttpHeader("key", "value"));

        var originalConfiguration = new TestConfiguration(SecurityProtocol.SASL_SSL, ssl, securityConfiguration, consumer, auth, headers);
        evaluator = new TestConfigurationEvaluator(originalConfiguration);

        TestObserver<TestConfiguration> testObserver = evaluator.eval(new DefaultExecutionContext()).test();

        testObserver.assertComplete();

        testObserver.assertValue(testConfiguration -> {
            assertThat(testConfiguration.getConsumer().isEnabled()).isTrue();
            assertThat(testConfiguration.getConsumer().getAutoOffsetReset()).isEqualTo("none");
            assertThat(testConfiguration.getConsumer().getAttributes()).isEqualTo(List.of("attribute1"));
            assertThat(testConfiguration.getConsumer().getTopics()).isEqualTo(Set.of("topic1"));
            assertThat(testConfiguration.getConsumer().getTopicPattern()).isEqualTo("topic-pattern");
            assertThat(testConfiguration.getConsumer().getTrustStore().getKey()).isEqualTo("my-key");
            assertThat(testConfiguration.getSecurity().getProperty()).isEqualTo("my-prop");
            assertThat(testConfiguration.getSsl().getKeyStore().getKey()).isEqualTo("keystore-key");
            assertThat(testConfiguration.getSsl().getTimeout()).isEqualTo(10L);
            assertThat(testConfiguration.getAuth()).isEqualTo(auth);
            assertThat(testConfiguration.getHeaders()).isEqualTo(headers);
            return true;
        });
    }

    static class DefaultTemplateContext implements TemplateContext {

        Map<String, Object> variables = new HashMap<>();

        @Override
        public void setVariable(String s, Object o) {
            variables.put(s, o);
        }

        @Override
        public void setDeferredVariable(String s, Completable completable) {}

        @Override
        public void setDeferredVariable(String s, Maybe<?> maybe) {}

        @Override
        public void setDeferredVariable(String s, Single<?> single) {}

        @Override
        public Object lookupVariable(String s) {
            return variables.get(s);
        }
    }

    static class DefaultTemplateEngine implements TemplateEngine {

        @Override
        public <T> T getValue(String s, Class<T> aClass) {
            return (T) s;
        }

        @Override
        public <T> Maybe<T> eval(String s, Class<T> aClass) {
            return Maybe.just((T) s);
        }

        @Override
        public TemplateContext getTemplateContext() {
            return new DefaultTemplateContext();
        }
    }

    static class DefaultExecutionContext implements HttpPlainExecutionContext {

        TemplateEngine templateEngine;

        Map<String, Object> attributes;

        Map<String, Object> internalAttributes;

        DefaultExecutionContext() {
            this(new DefaultTemplateEngine());
        }

        DefaultExecutionContext(TemplateEngine templateEngine) {
            this(templateEngine, new HashMap<>());
        }

        DefaultExecutionContext(TemplateEngine templateEngine, Map<String, Object> attributes) {
            this.templateEngine = templateEngine;
            this.attributes = attributes;
            this.internalAttributes = new HashMap<>();
        }

        @Override
        public Metrics metrics() {
            return null;
        }

        @Override
        public <T> T getComponent(Class<T> aClass) {
            return null;
        }

        @Override
        public void setAttribute(String s, Object o) {
            attributes.put(s, o);
        }

        @Override
        public void putAttribute(String s, Object o) {
            attributes.put(s, o);
        }

        @Override
        public void removeAttribute(String s) {
            attributes.remove(s);
        }

        @Override
        public <T> T getAttribute(String s) {
            return (T) attributes.get(s);
        }

        @Override
        public <T> List<T> getAttributeAsList(String s) {
            var value = this.attributes.get(s);

            if (value == null) {
                return null;
            }
            if (value instanceof List) {
                return (List<T>) value;
            }
            if (value instanceof String) {
                return (List<T>) Arrays.stream(((String) value).split(",")).toList();
            }

            return List.of((T) value);
        }

        @Override
        public Set<String> getAttributeNames() {
            return Set.of();
        }

        @Override
        public <T> Map<String, T> getAttributes() {
            return (Map<String, T>) attributes;
        }

        @Override
        public void setInternalAttribute(String s, Object o) {
            internalAttributes.put(s, o);
        }

        @Override
        public void putInternalAttribute(String s, Object o) {}

        @Override
        public void removeInternalAttribute(String s) {}

        @Override
        public <T> T getInternalAttribute(String s) {
            return (T) internalAttributes.get(s);
        }

        @Override
        public <T> Map<String, T> getInternalAttributes() {
            return (Map<String, T>) internalAttributes;
        }

        @Override
        public TemplateEngine getTemplateEngine() {
            return templateEngine;
        }

        @Override
        public Tracer getTracer() {
            return null;
        }

        @Override
        public long timestamp() {
            return 0;
        }

        @Override
        public String remoteAddress() {
            return "";
        }

        @Override
        public String localAddress() {
            return "";
        }

        @Override
        public TlsSession tlsSession() {
            return null;
        }

        @Override
        public HttpPlainRequest request() {
            return null;
        }

        @Override
        public HttpPlainResponse response() {
            return null;
        }

        @Override
        public Completable interrupt() {
            return null;
        }

        @Override
        public Completable interruptWith(ExecutionFailure executionFailure) {
            return Completable.defer(() -> {
                internalAttributes.put(InternalContextAttributes.ATTR_INTERNAL_EXECUTION_FAILURE, executionFailure);
                return Completable.error(new IllegalStateException(executionFailure.message()));
            });
        }

        @Override
        public Maybe<Buffer> interruptBody() {
            return null;
        }

        @Override
        public Maybe<Buffer> interruptBodyWith(ExecutionFailure executionFailure) {
            return null;
        }
    }
}
