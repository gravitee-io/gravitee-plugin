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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.gravitee.gateway.reactive.api.context.ExecutionContext;
import io.gravitee.plugin.annotation.processor.result.KeyStore;
import io.gravitee.plugin.annotation.processor.result.SecurityConfiguration;
import io.gravitee.plugin.annotation.processor.result.SecurityProtocol;
import io.gravitee.plugin.annotation.processor.result.Ssl;
import io.gravitee.plugin.annotation.processor.result.TestConfiguration;
import io.gravitee.plugin.annotation.processor.result.TestConfigurationEvaluator;
import io.gravitee.plugin.annotation.processor.result.TrustStore;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.observers.TestObserver;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    ExecutionContext.TemplateEngine templateEngine;

    @Before
    public void before() {
        TestConfiguration configuration = new TestConfiguration();
        configuration.getConsumer().setEnabled(true);
        configuration.getConsumer().setAutoOffsetReset("none");
        evaluator = new TestConfigurationEvaluator(configuration);
    }

    @Test
    public void should_interrupt_with_error_on_validation() {
        when(templateEngine.eval("none", String.class)).thenReturn(Maybe.just("result"));
        var context = new ExecutionContext(templateEngine);

        TestObserver<TestConfiguration> testObserver = evaluator.eval(context).test();

        testObserver.assertError(throwable -> {
            assertThat(throwable).isInstanceOf(IllegalStateException.class);
            assertThat(throwable.getMessage()).isEqualTo("Invalid configuration");
            return true;
        });
    }

    @Test
    public void should_return_evaluated_configuration() {
        when(templateEngine.eval("none", String.class)).thenReturn(Maybe.just("latest"));
        var context = new ExecutionContext(
            templateEngine,
            Map.ofEntries(
                Map.entry("gravitee.attributes.endpoint.test.protocol", "SSL"),
                Map.entry("gravitee.attributes.endpoint.test.consumer.topics", "topic1,topic2")
            )
        );

        evaluator
            .eval(context)
            .test()
            .assertComplete()
            .assertValue(testConfiguration -> {
                assertThat(testConfiguration.getConsumer().getAutoOffsetReset()).isEqualTo("latest");
                assertThat(testConfiguration.getProtocol()).isEqualTo(SecurityProtocol.SSL);
                assertThat(testConfiguration.getConsumer().getTopics()).isEqualTo(Set.of("topic1", "topic2"));
                return true;
            });
    }

    @Test
    public void should_cache_evaluated_configuration() {
        when(templateEngine.eval("none", String.class)).thenReturn(Maybe.just("latest"));
        var context = new ExecutionContext(
            templateEngine,
            Map.ofEntries(
                Map.entry("gravitee.attributes.endpoint.test.protocol", "SSL"),
                Map.entry("gravitee.attributes.endpoint.test.consumer.topics", "topic1,topic2")
            )
        );
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
        var spiedContext = spy(new ExecutionContext());
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

        var originalConfiguration = new TestConfiguration(SecurityProtocol.SASL_SSL, ssl, securityConfiguration, consumer);
        evaluator = new TestConfigurationEvaluator(originalConfiguration);

        TestObserver<TestConfiguration> testObserver = evaluator.eval(new ExecutionContext()).test();

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
            return true;
        });
    }
}
