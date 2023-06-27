/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.plugin.annotation.processor.result;

import io.gravitee.gateway.reactive.api.ExecutionFailure;
import io.gravitee.gateway.reactive.api.context.ExecutionContext;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestConfigurationEvaluator {

    private static final String FAILURE_CONFIGURATION_INVALID = "FAILURE_CONFIGURATION_INVALID";

    private final Logger logger = LoggerFactory.getLogger(TestConfigurationEvaluator.class);

    private final TestConfiguration configuration;

    private static final Validator validator;

    private final String attributePrefix = "gravitee.attributes.endpoint.test";

    private final String internalId;

    static {
        ValidatorFactory factory = Validation
            .byProvider(HibernateValidator.class)
            .configure()
            .messageInterpolator(new ParameterMessageInterpolator())
            .buildValidatorFactory();
        validator = factory.getValidator();
    }

    public TestConfigurationEvaluator(TestConfiguration configuration) {
        this.configuration = configuration;
        this.internalId = UUID.randomUUID().toString();
    }

    // Utility methods
    private String buildAttributeName(String attributePrefix, String name) {
        return attributePrefix.concat(".").concat(name);
    }

    private Maybe<String> evalStringProperty(String name, String value, String attributePrefix, ExecutionContext ctx) {
        //First check attributes
        String attribute = ctx.getAttribute(buildAttributeName(attributePrefix, name));
        if (attribute != null) {
            //If a value is found for this attribute, override the original value with it
            value = attribute;
        }

        //Then check EL
        return ctx.getTemplateEngine().eval(value, String.class);
    }

    private <T extends Enum<T>> T evalEnumProperty(String name, T value, Class<T> enumClass, String attributePrefix, ExecutionContext ctx) {
        String attribute = ctx.getAttribute(buildAttributeName(attributePrefix, name));
        if (attribute != null) {
            return T.valueOf(enumClass, attribute);
        }
        return value;
    }

    private Integer evalIntegerProperty(String name, int value, String attributePrefix, ExecutionContext ctx) {
        Integer attribute = ctx.getAttribute(buildAttributeName(attributePrefix, name));
        if (attribute != null) {
            return attribute;
        }
        return value;
    }

    private Long evalLongProperty(String name, long value, String attributePrefix, ExecutionContext ctx) {
        Long attribute = ctx.getAttribute(buildAttributeName(attributePrefix, name));
        if (attribute != null) {
            return attribute;
        }
        return value;
    }

    private boolean evalBooleanProperty(String name, boolean value, String attributePrefix, ExecutionContext ctx) {
        Object attribute = ctx.getAttribute(buildAttributeName(attributePrefix, name));
        if (attribute != null) {
            return (boolean) attribute;
        }
        return value;
    }

    private <T> Set<T> evalSetProperty(String name, Set<T> value, String attributePrefix, ExecutionContext ctx) {
        //Try to get a Set and if it fails try to get a String and split it
        try {
            Set<T> attribute = ctx.getAttribute(buildAttributeName(attributePrefix, name));
            if (attribute != null) {
                return attribute;
            }
        } catch (ClassCastException cce) {
            List<T> attribute = ctx.getAttributeAsList(buildAttributeName(attributePrefix, name));
            if (attribute != null) {
                return Set.copyOf(attribute);
            }
        }

        return value;
    }

    private <T> List<T> evalListProperty(String name, List<T> value, String attributePrefix, ExecutionContext ctx) {
        List<T> attribute = ctx.getAttributeAsList(buildAttributeName(attributePrefix, name));
        if (attribute != null) {
            return attribute;
        }

        return value;
    }

    private <T> void validateConfiguration(T configuration) {
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(configuration);

        if (!constraintViolations.isEmpty()) {
            StringBuilder exceptionMessage = new StringBuilder(constraintViolations.size() + " constraint violations found : ");
            constraintViolations.forEach(violation ->
                exceptionMessage
                    .append("[attribute[")
                    .append(violation.getPropertyPath())
                    .append("] reason[")
                    .append(violation.getMessage())
                    .append("]]")
            );

            //LOG
            logger.error(exceptionMessage.toString());

            //Throw exception with info
            throw new IllegalStateException(exceptionMessage.toString());
        }
    }

    public TestConfiguration evalNow(ExecutionContext ctx) {
        return eval(ctx).blockingGet();
    }

    public Single<TestConfiguration> eval(ExecutionContext ctx) {
        //First check if the configuration has not been already evaluated
        TestConfiguration evaluatedConf = ctx.getInternalAttribute("testConfiguration-" + this.internalId);
        if (evaluatedConf != null) {
            return Single.just(evaluatedConf);
        }

        TestConfiguration evaluatedConfiguration = new TestConfiguration();
        String currentAttributePrefix = attributePrefix;

        List<Maybe<String>> toEval = new ArrayList<>();

        //Field protocol
        evaluatedConfiguration.setProtocol(
            evalEnumProperty(
                "protocol",
                configuration.getProtocol(),
                io.gravitee.plugin.annotation.processor.result.SecurityProtocol.class,
                currentAttributePrefix,
                ctx
            )
        );

        //Consumer section begin
        if (evaluatedConfiguration.getConsumer() != null) {
            currentAttributePrefix = attributePrefix.concat(".consumer");
            //Field enabled
            evaluatedConfiguration
                .getConsumer()
                .setEnabled(evalBooleanProperty("enabled", configuration.getConsumer().isEnabled(), currentAttributePrefix, ctx));
            //Field autoOffsetReset
            toEval.add(
                evalStringProperty("autoOffsetReset", configuration.getConsumer().getAutoOffsetReset(), currentAttributePrefix, ctx)
                    .doOnSuccess(value -> evaluatedConfiguration.getConsumer().setAutoOffsetReset(value))
            );
            //Field topics
            evaluatedConfiguration
                .getConsumer()
                .setTopics(evalSetProperty("topics", configuration.getConsumer().getTopics(), currentAttributePrefix, ctx));
            //Field attributes
            evaluatedConfiguration
                .getConsumer()
                .setAttributes(evalListProperty("attributes", configuration.getConsumer().getAttributes(), currentAttributePrefix, ctx));

            //trustStore section begin
            if (evaluatedConfiguration.getConsumer().getTrustStore() != null) {
                currentAttributePrefix = attributePrefix.concat(".consumer.trustStore");
                //Field key
                toEval.add(
                    evalStringProperty("key", configuration.getConsumer().getTrustStore().getKey(), currentAttributePrefix, ctx)
                        .doOnSuccess(value -> evaluatedConfiguration.getConsumer().getTrustStore().setKey(value))
                );
            }
            //trustStore section end

        }
        //Consumer section end

        //ssl section begin
        if (evaluatedConfiguration.getSsl() != null) {
            currentAttributePrefix = attributePrefix.concat(".ssl");
            //Field timeout
            evaluatedConfiguration
                .getSsl()
                .setTimeout(evalLongProperty("timeout", configuration.getSsl().getTimeout(), currentAttributePrefix, ctx));

            //keyStore section begin
            if (evaluatedConfiguration.getSsl().getKeyStore() != null) {
                currentAttributePrefix = attributePrefix.concat(".ssl.keyStore");
                //Field key
                toEval.add(
                    evalStringProperty("key", configuration.getSsl().getKeyStore().getKey(), currentAttributePrefix, ctx)
                        .doOnSuccess(value -> evaluatedConfiguration.getSsl().getKeyStore().setKey(value))
                );
            }
            //keyStore section end

        }
        //ssl section end

        //security section begin
        if (evaluatedConfiguration.getSecurity() != null) {
            currentAttributePrefix = attributePrefix.concat(".security");
            //Field property
            toEval.add(
                evalStringProperty("property", configuration.getSecurity().getProperty(), currentAttributePrefix, ctx)
                    .doOnSuccess(value -> evaluatedConfiguration.getSecurity().setProperty(value))
            );
        }
        //security section end

        // Evaluate properties that needs EL, validate evaluatedConf and returns it
        return Maybe
            .merge(Flowable.fromIterable(toEval))
            .ignoreElements()
            .andThen(Completable.fromRunnable(() -> validateConfiguration(evaluatedConfiguration)))
            .andThen(
                Completable.fromRunnable(() -> ctx.setInternalAttribute("testConfiguration-" + this.internalId, evaluatedConfiguration))
            )
            .onErrorResumeWith(
                ctx.interruptWith(new ExecutionFailure(500).message("Invalid configuration").key(FAILURE_CONFIGURATION_INVALID))
            )
            .toSingle(() -> evaluatedConfiguration);
    }
}
