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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.gravitee.gateway.reactive.api.ExecutionFailure;
import io.gravitee.gateway.reactive.api.context.DeploymentContext;
import io.gravitee.gateway.reactive.api.context.base.BaseExecutionContext;
import io.gravitee.gateway.reactive.api.context.http.HttpPlainExecutionContext;
import io.gravitee.secrets.api.el.FieldKind;
import io.gravitee.secrets.api.el.SecretFieldAccessControl;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TestConfigurationEvaluator {

    private static final String FAILURE_CONFIGURATION_INVALID = "FAILURE_CONFIGURATION_INVALID";

    private final Logger logger = LoggerFactory.getLogger(TestConfigurationEvaluator.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final TestConfiguration configuration;

    private static final Validator validator;

    private final String attributePrefix = "gravitee.attributes.endpoint.test";

    private final String internalId;

    static {
        ValidatorFactory factory = Validation.byProvider(HibernateValidator.class).configure()
                .messageInterpolator(new ParameterMessageInterpolator()).buildValidatorFactory();
        validator = factory.getValidator();
    }

    public TestConfigurationEvaluator (
            TestConfiguration configuration)
    {
        this.configuration = configuration;
        this.internalId = UUID.randomUUID().toString();
    }

    // Utility methods
    private String buildAttributeName(String attributePrefix, String name) {
        return attributePrefix.concat(".").concat(name);
    }

    private String buildFieldName(String attributeName) {
        return attributeName.substring(this.attributePrefix.length()+1);
    }

    private Maybe<String> evalStringProperty(String name, String value, String attributePrefix, BaseExecutionContext ctx, String secretKind) {
        //First check attributes
        String attributeName = buildAttributeName(attributePrefix, name);
        String attribute = ctx.getAttribute(attributeName);
        if (attribute != null) {
            //If a value is found for this attribute, override the original value with it
            value = attribute;
        }
        //If value is null, return empty
        if(value == null) {
            return Maybe.empty();
        }

        SecretFieldAccessControl accessControl;
        if(secretKind.isEmpty()) {
            accessControl = new SecretFieldAccessControl(false, null, null);
        } else {
            accessControl = new SecretFieldAccessControl(true, FieldKind.valueOf(secretKind), buildFieldName(attributeName));
        }

        //Then check EL
        String finalValue = value;
        return ctx
                .getTemplateEngine()
                .eval(value, String.class)
                .doOnSubscribe(d -> ctx.getTemplateEngine().getTemplateContext().setVariable(SecretFieldAccessControl.EL_VARIABLE, accessControl))
                .doOnTerminate(() -> ctx.getTemplateEngine().getTemplateContext().setVariable(SecretFieldAccessControl.EL_VARIABLE, null))
                .doOnError(throwable -> logger.error("Unable to evaluate property [{}] with expression [{}].", name, finalValue));
    }

    private Maybe<String> evalStringProperty(String name, String value, String attributePrefix, DeploymentContext ctx, String secretKind) {
        //If value is null, return empty
        if(value == null) {
            return Maybe.empty();
        }

        SecretFieldAccessControl accessControl;
        String property = buildFieldName(buildAttributeName(attributePrefix, name));
        if(secretKind.isEmpty()) {
            accessControl = new SecretFieldAccessControl(false, null, null);
        } else {
            accessControl = new SecretFieldAccessControl(true, FieldKind.valueOf(secretKind), property);
        }

        //Then check EL
        return ctx
                .getTemplateEngine()
                .eval(value, String.class)
                .doOnSubscribe(d -> ctx.getTemplateEngine().getTemplateContext().setVariable(SecretFieldAccessControl.EL_VARIABLE, accessControl))
                .doOnTerminate(() -> ctx.getTemplateEngine().getTemplateContext().setVariable(SecretFieldAccessControl.EL_VARIABLE, null))
                .doOnError(throwable -> logger.error("Unable to evaluate property [{}] with expression [{}].", property, value));
    }

    private <T extends Enum<T>> T evalEnumProperty(String name, T value, Class<T> enumClass, String attributePrefix, BaseExecutionContext ctx) {
        String attribute = ctx.getAttribute(buildAttributeName(attributePrefix, name));
        if (attribute != null) {
            return T.valueOf(enumClass, attribute);
        }
        return value;
    }

    private Integer evalIntegerProperty(String name, int value, String attributePrefix, BaseExecutionContext ctx) {
        Integer attribute = ctx.getAttribute(buildAttributeName(attributePrefix, name));
        if (attribute != null) {
            return attribute;
        }
        return value;
    }

    private Long evalLongProperty(String name, long value, String attributePrefix, BaseExecutionContext ctx) {
        Long attribute = ctx.getAttribute(buildAttributeName(attributePrefix, name));
        if (attribute != null) {
            return attribute;
        }
        return value;
    }

    private boolean evalBooleanProperty(String name, boolean value, String attributePrefix, BaseExecutionContext ctx) {
        Object attribute = ctx.getAttribute(buildAttributeName(attributePrefix, name));
        if (attribute != null) {
            return (boolean) attribute;
        }
        return value;
    }

    private <T> Set<T> evalSetProperty(String name, Set<T> value, String attributePrefix, BaseExecutionContext ctx) {
        //Try to get a Set and if it fails try to get a String and split it
        try {
            Set<T> attribute = ctx.getAttribute(buildAttributeName(attributePrefix, name));
            if (attribute != null) {
                return attribute;
            }
        } catch (ClassCastException cce) {
            List<T> attribute = ctx.getAttributeAsList(buildAttributeName(attributePrefix, name));
            if(attribute != null) {
                return Set.copyOf(attribute);
            }
        }

        return value;
    }

    private <T> List<T> evalListProperty(String name, List<T> value, String attributePrefix, BaseExecutionContext ctx) {
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

    /**
     * Blocking eval method (see {@link #eval(BaseExecutionContext)} Eval})
     * <b>Caution when using this method if the evaluation involves EL expressions that trigger blocking fetches</b>
     * @param ctx the current context
     * @return configuration with all dynamic configuration parameters updated
     */
    public TestConfiguration evalNow(BaseExecutionContext ctx) {
        return eval(ctx).blockingGet();
    }

    public TestConfiguration evalNow(DeploymentContext ctx) {
        return eval(ctx).blockingGet();
    }

    public Single<TestConfiguration> eval(BaseExecutionContext ctx) {
        return eval(ctx, null);
    }

    public Single<TestConfiguration> eval(DeploymentContext ctx) {
        return eval(null, ctx);
    }

    /**
     * Evaluates the configuration using the context to update parameters using attributes or EL
     * and stores it as an internal attributes to avoid multiple evaluation
     * @param ctx the current context
     * @return configuration with all dynamic configuration parameters updated
     */
    private Single<TestConfiguration> eval(BaseExecutionContext baseExecutionContext, DeploymentContext deploymentContext) {

        if(baseExecutionContext != null) {
            if (attributePrefix.isEmpty()) {
                return Single.error(new IllegalArgumentException("@ConfigurationEvaluator(attributePrefix=\"...\") is required when using BaseExecutionContext."));
            }
            //First check if the configuration has not been already evaluated
            TestConfiguration evaluatedConf = baseExecutionContext.getInternalAttribute("testConfiguration-"+this.internalId);
            if(evaluatedConf != null) {
                return Single.just(evaluatedConf);
            }
        }

        TestConfiguration evaluatedConfiguration;
        try {
            evaluatedConfiguration = objectMapper.readValue(objectMapper.writeValueAsString(configuration), TestConfiguration.class);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            logger.error("Unable to clone configuration", e);
            return Single.error(e);
        }

        String currentAttributePrefix = attributePrefix;

        List<Maybe<String>> toEval = new ArrayList<>();
        //Field protocol
        if(baseExecutionContext != null) {
            evaluatedConfiguration.setProtocol(
                    evalEnumProperty("protocol", configuration.getProtocol(), io.gravitee.plugin.annotation.processor.result.SecurityProtocol.class, currentAttributePrefix, baseExecutionContext)
            );
        } else if(deploymentContext != null) {
        }

        //Consumer section begin
        if(evaluatedConfiguration.getConsumer() != null) {
            currentAttributePrefix = attributePrefix.concat(".consumer");
            //Field enabled
            if(baseExecutionContext != null) {
                evaluatedConfiguration.getConsumer().setEnabled(
                        evalBooleanProperty("enabled", configuration.getConsumer().isEnabled(), currentAttributePrefix, baseExecutionContext)
                );
            } else if(deploymentContext != null) {
            }
            //Field autoOffsetReset
            if(baseExecutionContext != null) {
                toEval.add(
                    evalStringProperty("autoOffsetReset", configuration.getConsumer().getAutoOffsetReset(), currentAttributePrefix, baseExecutionContext, "")
                                .doOnSuccess(value -> evaluatedConfiguration.getConsumer().setAutoOffsetReset(value))
                );
            } else if(deploymentContext != null) {
                toEval.add(
                        evalStringProperty("autoOffsetReset", configuration.getConsumer().getAutoOffsetReset(), currentAttributePrefix, deploymentContext, "")
                                .doOnSuccess(value -> evaluatedConfiguration.getConsumer().setAutoOffsetReset(value)));
            }
            //Field topics
            if(baseExecutionContext != null) {
                evaluatedConfiguration.getConsumer().setTopics(
                        evalSetProperty("topics", configuration.getConsumer().getTopics(), currentAttributePrefix, baseExecutionContext)
                );
            } else if(deploymentContext != null) {
            }
            //Field topicPattern
            if(baseExecutionContext != null) {
                toEval.add(
                        evalStringProperty("topicPattern", configuration.getConsumer().getTopicPattern(), currentAttributePrefix, baseExecutionContext, "")
                                .doOnSuccess(value -> evaluatedConfiguration.getConsumer().setTopicPattern(value))
                );
            } else if(deploymentContext != null) {
                toEval.add(
                        evalStringProperty("topicPattern", configuration.getConsumer().getTopicPattern(), currentAttributePrefix, deploymentContext, "")
                                .doOnSuccess(value -> evaluatedConfiguration.getConsumer().setTopicPattern(value)));
            }
            //Field attributes
            if(baseExecutionContext != null) {
                evaluatedConfiguration.getConsumer().setAttributes(
                        evalListProperty("attributes", configuration.getConsumer().getAttributes(), currentAttributePrefix, baseExecutionContext)
                );
            } else if(deploymentContext != null) {
            }

            //trustStore section begin
            if(evaluatedConfiguration.getConsumer().getTrustStore() != null) {
                currentAttributePrefix = attributePrefix.concat(".consumer.trustStore");
                //Field key
                if(baseExecutionContext != null) {
                    toEval.add(
                            evalStringProperty("key", configuration.getConsumer().getTrustStore().getKey(), currentAttributePrefix, baseExecutionContext, "PRIVATE_KEY")
                                    .doOnSuccess(value -> evaluatedConfiguration.getConsumer().getTrustStore().setKey(value))
                    );
                } else if(deploymentContext != null) {
                    toEval.add(
                            evalStringProperty("key", configuration.getConsumer().getTrustStore().getKey(), currentAttributePrefix, deploymentContext, "PRIVATE_KEY")
                                    .doOnSuccess(value -> evaluatedConfiguration.getConsumer().getTrustStore().setKey(value)));
                }

            }
            //trustStore section end

        }
        //Consumer section end

        //ssl section begin
        if(evaluatedConfiguration.getSsl() != null) {
            currentAttributePrefix = attributePrefix.concat(".ssl");
            //Field timeout
            if(baseExecutionContext != null) {
                evaluatedConfiguration.getSsl().setTimeout(
                        evalLongProperty("timeout", configuration.getSsl().getTimeout(), currentAttributePrefix, baseExecutionContext)
                );
            } else if(deploymentContext != null) {
            }

            //keyStore section begin
            if(evaluatedConfiguration.getSsl().getKeyStore() != null) {
                currentAttributePrefix = attributePrefix.concat(".ssl.keyStore");
                //Field key
                if(baseExecutionContext != null) {
                    toEval.add(
                            evalStringProperty("key", configuration.getSsl().getKeyStore().getKey(), currentAttributePrefix, baseExecutionContext, "PRIVATE_KEY")
                                    .doOnSuccess(value -> evaluatedConfiguration.getSsl().getKeyStore().setKey(value))
                    );
                } else if(deploymentContext != null) {
                    toEval.add(
                            evalStringProperty("key", configuration.getSsl().getKeyStore().getKey(), currentAttributePrefix, deploymentContext, "PRIVATE_KEY")
                                    .doOnSuccess(value -> evaluatedConfiguration.getSsl().getKeyStore().setKey(value)));
                }

            }
            //keyStore section end

        }
        //ssl section end

        //security section begin
        if(evaluatedConfiguration.getSecurity() != null) {
            currentAttributePrefix = attributePrefix.concat(".security");
            //Field property
            if(baseExecutionContext != null) {
                toEval.add(
                        evalStringProperty("property", configuration.getSecurity().getProperty(), currentAttributePrefix, baseExecutionContext, "")
                                .doOnSuccess(value -> evaluatedConfiguration.getSecurity().setProperty(value))
                );
            } else if(deploymentContext != null) {
                toEval.add(
                        evalStringProperty("property", configuration.getSecurity().getProperty(), currentAttributePrefix, deploymentContext, "")
                                .doOnSuccess(value -> evaluatedConfiguration.getSecurity().setProperty(value)));
            }

        }
        //security section end

        // Evaluate properties that needs EL, validate evaluatedConf and returns it
        return Maybe
                .concat(Flowable.fromIterable(toEval))
                .ignoreElements()
                .andThen(Completable.fromRunnable(() -> validateConfiguration(evaluatedConfiguration)))
                .andThen(Completable.fromRunnable(() -> {
                    if(baseExecutionContext != null) {
                        baseExecutionContext.setInternalAttribute("testConfiguration-"+this.internalId, evaluatedConfiguration);
                    }
                }))
                .onErrorResumeNext(t -> {
                    if(baseExecutionContext != null) {
                        return ((HttpPlainExecutionContext)baseExecutionContext).interruptWith(new ExecutionFailure(500).message("Invalid configuration").key(FAILURE_CONFIGURATION_INVALID));
                    }
                    return Completable.error(t);
                })
                .toSingle(() -> evaluatedConfiguration);
    }
}
