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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.gravitee.gateway.reactive.api.ExecutionFailure;
import io.gravitee.gateway.reactive.api.context.ExecutionContext;
import io.gravitee.plugin.annotation.processor.result.SecurityProtocol;
import io.gravitee.plugin.annotation.processor.result.TestConfiguration;
import io.gravitee.plugin.annotation.processor.result.TestConfigurationEvaluator;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.observers.TestObserver;
import java.util.List;
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
    ExecutionContext context;

    @Mock
    ExecutionContext.TemplateEngine templateEngine;

    @Before
    public void before() {
        TestConfiguration configuration = new TestConfiguration();
        configuration.getConsumer().setEnabled(true);
        configuration.getConsumer().setAutoOffsetReset("none");
        evaluator = new TestConfigurationEvaluator(configuration);

        when(context.getTemplateEngine()).thenReturn(templateEngine);

        when(context.interruptWith(any()))
            .thenAnswer(invocation ->
                Completable.defer(() ->
                    Completable.error(new IllegalStateException(((ExecutionFailure) invocation.getArgument(0)).message()))
                )
            );
    }

    @Test
    public void should_interrupt_with_error_on_validation() {
        when(templateEngine.eval("none", String.class)).thenReturn(Maybe.just("result"));

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
        when(context.getAttribute("gravitee.attributes.endpoint.test.protocol")).thenReturn("SSL");
        when(context.getAttribute("gravitee.attributes.endpoint.test.consumer.topics")).thenReturn("topic1,topic2");
        when(context.getAttributeAsList("gravitee.attributes.endpoint.test.consumer.topics")).thenReturn(List.of("topic1", "topic2"));

        TestObserver<TestConfiguration> testObserver = evaluator.eval(context).test();

        testObserver.assertComplete();

        testObserver.assertValue(testConfiguration -> {
            assertThat(testConfiguration.getConsumer().getAutoOffsetReset()).isEqualTo("latest");
            assertThat(testConfiguration.getProtocol()).isEqualTo(SecurityProtocol.SSL);
            assertThat(testConfiguration.getConsumer().getTopics()).isEqualTo(Set.of("topic1", "topic2"));
            return true;
        });

        verify(context).getAttributeAsList("gravitee.attributes.endpoint.test.consumer.topics");
        verify(context).setInternalAttribute(anyString(), any(TestConfiguration.class));
    }

    @Test
    public void should_return_evaluated_configuration_from_internal_attribute() {
        TestConfiguration configuration = new TestConfiguration();
        configuration.getConsumer().setAutoOffsetReset("earliest");
        when(context.getInternalAttribute(anyString())).thenReturn(configuration);

        TestObserver<TestConfiguration> testObserver = evaluator.eval(context).test();

        testObserver.assertComplete();

        testObserver.assertValue(testConfiguration -> {
            assertThat(testConfiguration.getConsumer().getAutoOffsetReset()).isEqualTo("earliest");
            return true;
        });

        verify(context, times(0)).setInternalAttribute(anyString(), any(TestConfiguration.class));
    }
}
