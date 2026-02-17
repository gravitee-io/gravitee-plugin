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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gravitee.common.http.HttpHeader;
import io.gravitee.gateway.reactive.api.ExecutionFailure;
import io.gravitee.gateway.reactive.api.context.DeploymentContext;
import io.gravitee.gateway.reactive.api.context.base.BaseExecutionContext;
import io.gravitee.gateway.reactive.api.context.http.HttpPlainExecutionContext;
import io.gravitee.node.logging.NodeLoggerFactory;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class TestConfigurationEvaluator {

    private static final String FAILURE_CONFIGURATION_INVALID = "FAILURE_CONFIGURATION_INVALID";

    private final Logger log = NodeLoggerFactory.getLogger(TestConfigurationEvaluator.class);

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
                .doOnError(throwable -> ctx.withLogger(log).error("Unable to evaluate property [{}] with expression [{}].", name, finalValue, throwable));
    }

    private Maybe<String> evalStringProperty(String name, String value, String attributePrefix, DeploymentContext ctx, String secretKind) {
        // If value is null, return empty
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
                .doOnError(throwable -> log.error("Unable to evaluate property [{}] with expression [{}].", property, value, throwable));
    }

    private <T extends Enum<T>> T evalEnumProperty(String name, T value, Class<T> enumClass, String attributePrefix, BaseExecutionContext ctx) {
        String attribute = ctx.getAttribute(buildAttributeName(attributePrefix, name));
        if (attribute != null) {
            return T.valueOf(enumClass, attribute);
        }
        return value;
    }

    private Integer evalIntegerProperty(String name, Integer value, String attributePrefix, BaseExecutionContext ctx) {
        Integer attribute = ctx.getAttribute(buildAttributeName(attributePrefix, name));
        if (attribute != null) {
            return attribute;
        }
        return value;
    }

    private Long evalLongProperty(String name, Long value, String attributePrefix, BaseExecutionContext ctx) {
        Long attribute = ctx.getAttribute(buildAttributeName(attributePrefix, name));
        if (attribute != null) {
            return attribute;
        }
        return value;
    }

    private Double evalDoubleProperty(String name, Double value, String attributePrefix, BaseExecutionContext ctx) {
        Double attribute = ctx.getAttribute(buildAttributeName(attributePrefix, name));
        if (attribute != null) {
            return attribute;
        }
        return value;
    }

    private Boolean evalBooleanProperty(String name, Boolean value, String attributePrefix, BaseExecutionContext ctx) {
        Boolean attribute = ctx.getAttribute(buildAttributeName(attributePrefix, name));
        if (attribute != null) {
            return attribute;
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

    private Maybe<List<String>> evalListStringProperty(String name, List<String> value, String attributePrefix, BaseExecutionContext ctx) {
        List<String> attribute = ctx.getAttributeAsList(buildAttributeName(attributePrefix, name));
        if (attribute != null) {
            //If a value is found for this attribute, override the original value with it
            value = attribute;
        }
        //If value is null, return empty
        if(value == null) {
            return Maybe.empty();
        }

        //Then check EL
        return Flowable.fromIterable(value).filter(Objects::nonNull)
                .flatMapMaybe(v -> ctx
                        .getTemplateEngine()
                        .eval(v, String.class)
                        .doOnError(throwable -> ctx.withLogger(log).error("Unable to evaluate property [{}] with expression [{}].", name, v, throwable)))
                .toList()
                .toMaybe();
    }

    private Maybe<List<String>> evalListStringProperty(String name, List<String> value, String attributePrefix, DeploymentContext ctx) {
        //If value is null, return empty
        if(value == null) {
            return Maybe.empty();
        }

        //Then check EL
        return Flowable.fromIterable(value).filter(Objects::nonNull)
                .flatMapMaybe(v -> ctx
                        .getTemplateEngine()
                        .eval(v, String.class)
                        .doOnError(throwable -> log.error("Unable to evaluate property [{}] with expression [{}].", name, v, throwable)))
                .toList()
                .toMaybe();
    }

    private Maybe<List<HttpHeader>> evalListHeaderProperty(
            String name,
            List<HttpHeader> headers,
            String attributePrefix,
            BaseExecutionContext ctx
    ) {
        //First check attributes
        String attributeName = buildAttributeName(attributePrefix, name);
        List<HttpHeader> attribute = ctx.getAttribute(attributeName);
        if (attribute != null) {
            //If a value is found for this attribute, override the original value with it
            headers = attribute;
        }
        //If headers is null, return empty
        if (headers == null) {
            return Maybe.empty();
        }

        //Then check EL
        return Flowable
                .fromIterable(headers)
                .filter(Objects::nonNull)
                .flatMapMaybe(header -> {
                    SecretFieldAccessControl accessControl = new SecretFieldAccessControl(true, FieldKind.HEADER, header.getName());
                    return ctx
                            .getTemplateEngine()
                            .eval(header.getValue(), String.class)
                            .doOnSubscribe(d ->
                                    ctx.getTemplateEngine().getTemplateContext().setVariable(SecretFieldAccessControl.EL_VARIABLE, accessControl)
                            )
                            .doOnTerminate(() ->
                                    ctx.getTemplateEngine().getTemplateContext().setVariable(SecretFieldAccessControl.EL_VARIABLE, null)
                            )
                            .map(evaluatedValue -> new HttpHeader(header.getName(), evaluatedValue))
                            .doOnError(throwable -> ctx.withLogger(log).error("Unable to evaluate property [{}] with expression [{}].", name, header.getValue(), throwable)
                            );
                })
                .toList()
                .toMaybe();
    }

    private Maybe<List<HttpHeader>> evalListHeaderProperty(
            String name,
            List<HttpHeader> headers,
            String attributePrefix,
            DeploymentContext ctx
    ) {
        //If headers is null, return empty
        if (headers == null) {
            return Maybe.empty();
        }

        //Then check EL
        return Flowable
                .fromIterable(headers)
                .filter(Objects::nonNull)
                .flatMapMaybe(header -> {
                    SecretFieldAccessControl accessControl = new SecretFieldAccessControl(true, FieldKind.HEADER, header.getName());
                    return ctx
                            .getTemplateEngine()
                            .eval(header.getValue(), String.class)
                            .doOnSubscribe(d ->
                                    ctx.getTemplateEngine().getTemplateContext().setVariable(SecretFieldAccessControl.EL_VARIABLE, accessControl)
                            )
                            .doOnTerminate(() ->
                                    ctx.getTemplateEngine().getTemplateContext().setVariable(SecretFieldAccessControl.EL_VARIABLE, null)
                            )
                            .map(evaluatedValue -> new HttpHeader(header.getName(), evaluatedValue))
                            .doOnError(throwable -> log.error("Unable to evaluate property [{}] with expression [{}].", name, header.getValue(), throwable)
                            );
                })
                .toList()
                .toMaybe();
    }

    private Maybe<Map<String, String>> evalMapStringProperty(String name, Map<String, String> value, String attributePrefix, BaseExecutionContext ctx) {
        Map<String, String> attribute = ctx.getAttribute(buildAttributeName(attributePrefix, name));
        if (attribute != null) {
            //If a value is found for this attribute, override the original value with it
            value = attribute;
        }
        //If value is null, return empty
        if(value == null) {
            return Maybe.empty();
        }

        //Then check EL
        return Flowable.fromIterable(value.entrySet()).filter(Objects::nonNull)
                .flatMapMaybe(entry -> {
                    SecretFieldAccessControl accessControl = new SecretFieldAccessControl(true, FieldKind.GENERIC, entry.getKey());
                    return ctx
                            .getTemplateEngine()
                            .eval(entry.getValue(), String.class)
                            .doOnSubscribe(d ->
                                    ctx.getTemplateEngine().getTemplateContext().setVariable(SecretFieldAccessControl.EL_VARIABLE, accessControl)
                            )
                            .doOnTerminate(() ->
                                    ctx.getTemplateEngine().getTemplateContext().setVariable(SecretFieldAccessControl.EL_VARIABLE, null)
                            )
                            .map(evaluatedValue -> Map.entry(entry.getKey(), evaluatedValue))
                            .doOnError(throwable -> ctx.withLogger(log).error("Unable to evaluate property [{}] with expression [{}].", name, entry.getValue(), throwable)
                            );
                }).collect(() -> (Map<String, String>) new HashMap<String, String>(), (map, entry) -> map.put(entry.getKey(), entry.getValue()))
                .toMaybe();
    }

    private Maybe<Map<String, String>> evalMapStringProperty(String name, Map<String, String> value, String attributePrefix, DeploymentContext ctx) {
        //If value is null, return empty
        if(value == null) {
            return Maybe.empty();
        }

        //Then check EL
        return Flowable.fromIterable(value.entrySet()).filter(Objects::nonNull)
                .flatMapMaybe(entry -> {
                    SecretFieldAccessControl accessControl = new SecretFieldAccessControl(true, FieldKind.GENERIC, entry.getKey());
                    return ctx
                            .getTemplateEngine()
                            .eval(entry.getValue(), String.class)
                            .doOnSubscribe(d ->
                                    ctx.getTemplateEngine().getTemplateContext().setVariable(SecretFieldAccessControl.EL_VARIABLE, accessControl)
                            )
                            .doOnTerminate(() ->
                                    ctx.getTemplateEngine().getTemplateContext().setVariable(SecretFieldAccessControl.EL_VARIABLE, null)
                            )
                            .map(evaluatedValue -> Map.entry(entry.getKey(), evaluatedValue))
                            .doOnError(throwable -> log.error("Unable to evaluate property [{}] with expression [{}].", name, entry.getValue(), throwable)
                            );
                }).collect(() -> (Map<String, String>) new HashMap<String, String>(), (map, entry) -> map.put(entry.getKey(), entry.getValue()))
                .toMaybe();
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
            log.error(exceptionMessage.toString());

            //Throw exception with info
            throw new IllegalStateException(exceptionMessage.toString());
        }
    }

    /**
     * Blocking eval method (see {@link #eval(BaseExecutionContext)} Eval})
     * <b>Caution when using this method if the evaluation involves EL expressions that trigger blocking fetches</b>
     * @param ctx the current execution context
     * @return configuration with all dynamic configuration parameters updated
     */
    public TestConfiguration evalNow(BaseExecutionContext ctx) {
        return eval(ctx).blockingGet();
    }

    /**
     * Blocking eval method (see {@link #eval(DeploymentContext)} Eval})
     * <b>Caution when using this method if the evaluation involves EL expressions that trigger blocking fetches</b>
     * @param ctx the current deployment context
     * @return configuration with all dynamic configuration parameters updated
     */
    public TestConfiguration evalNow(DeploymentContext ctx) {
        return eval(ctx).blockingGet();
    }

    /**
     * Evaluates the configuration using the execution context to update parameters using attributes or EL
     * and stores it as an internal attributes to avoid multiple evaluation
     * @param ctx the current context
     * @return configuration with all dynamic configuration parameters updated
     */
    public Single<TestConfiguration> eval(BaseExecutionContext ctx) {
        return eval(ctx, null);
    }

    /**
     * Evaluates the configuration using a deployment context to update parameters using EL
     * and stores it as an internal attributes to avoid multiple evaluation
     * @param ctx the current deployment context
     * @return configuration with all dynamic configuration parameters updated
     */
    public Single<TestConfiguration> eval(DeploymentContext ctx) {
        return eval(null, ctx);
    }

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
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            evaluatedConfiguration = objectMapper.readValue(objectMapper.writeValueAsString(configuration), TestConfiguration.class);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            baseExecutionContext.withLogger(log).error("Unable to clone configuration", e);
            return Single.error(e);
        }

        String currentAttributePrefix = attributePrefix;

        List<Maybe<String>> toEval = new ArrayList<>();
        List<Maybe<List<String>>> toEvalList = new ArrayList<>();
        List<Maybe<List<HttpHeader>>> toEvalHeaderList = new ArrayList<>();
        List<Maybe<Map<String, String>>> toEvalMap = new ArrayList<>();
        //Field protocol
        if(baseExecutionContext != null) {
            evaluatedConfiguration.setProtocol(
                    evalEnumProperty("protocol", configuration.getProtocol(), io.gravitee.plugin.annotation.processor.result.SecurityProtocol.class, currentAttributePrefix, baseExecutionContext)
            );
        } else if(deploymentContext != null) {
        }
        //Field headers
        if(baseExecutionContext != null) {
            toEvalHeaderList.add(
                    evalListHeaderProperty("headers", configuration.getHeaders(), currentAttributePrefix, baseExecutionContext)
                            .doOnSuccess(value -> evaluatedConfiguration.setHeaders(value))
            );
        } else if(deploymentContext != null) {
            toEvalHeaderList.add(
                    evalListHeaderProperty("headers", configuration.getHeaders(), currentAttributePrefix, deploymentContext)
                            .doOnSuccess(value -> evaluatedConfiguration.setHeaders(value)));
        }
        //Field doubleValue
        if(baseExecutionContext != null) {
            evaluatedConfiguration.setDoubleValue(
                    evalDoubleProperty("doubleValue", configuration.getDoubleValue(), currentAttributePrefix, baseExecutionContext)
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
            //Field windowSize
            if(baseExecutionContext != null) {
                evaluatedConfiguration.getConsumer().setWindowSize(
                        evalIntegerProperty("windowSize", configuration.getConsumer().getWindowSize(), currentAttributePrefix, baseExecutionContext)
                );
            } else if(deploymentContext != null) {
            }
            //Field attributes
            if(baseExecutionContext != null) {
                toEvalList.add(
                        evalListStringProperty("attributes", configuration.getConsumer().getAttributes(), currentAttributePrefix, baseExecutionContext)
                                .doOnSuccess(value -> evaluatedConfiguration.getConsumer().setAttributes(value))
                );
            } else if(deploymentContext != null) {
                toEvalList.add(
                        evalListStringProperty("attributes", configuration.getConsumer().getAttributes(), currentAttributePrefix, deploymentContext)
                                .doOnSuccess(value -> evaluatedConfiguration.getConsumer().setAttributes(value)));
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

        //auth section begin
        if(evaluatedConfiguration.getAuth() != null) {
            currentAttributePrefix = attributePrefix.concat(".auth");
            //Field username
            if(baseExecutionContext != null) {
                toEval.add(
                        evalStringProperty("username", configuration.getAuth().getUsername(), currentAttributePrefix, baseExecutionContext, "")
                                .doOnSuccess(value -> evaluatedConfiguration.getAuth().setUsername(value))
                );
            } else if(deploymentContext != null) {
                toEval.add(
                        evalStringProperty("username", configuration.getAuth().getUsername(), currentAttributePrefix, deploymentContext, "")
                                .doOnSuccess(value -> evaluatedConfiguration.getAuth().setUsername(value)));
            }
            //Field password
            if(baseExecutionContext != null) {
                toEval.add(
                        evalStringProperty("password", configuration.getAuth().getPassword(), currentAttributePrefix, baseExecutionContext, "")
                                .doOnSuccess(value -> evaluatedConfiguration.getAuth().setPassword(value))
                );
            } else if(deploymentContext != null) {
                toEval.add(
                        evalStringProperty("password", configuration.getAuth().getPassword(), currentAttributePrefix, deploymentContext, "")
                                .doOnSuccess(value -> evaluatedConfiguration.getAuth().setPassword(value)));
            }

        }
        //auth section end

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

        //sslOptions section begin
        if(evaluatedConfiguration.getSslOptions() != null) {
            currentAttributePrefix = attributePrefix.concat(".sslOptions");
            //Field trustAll
            if(baseExecutionContext != null) {
                evaluatedConfiguration.getSslOptions().setTrustAll(
                        evalBooleanProperty("trustAll", configuration.getSslOptions().isTrustAll(), currentAttributePrefix, baseExecutionContext)
                );
            } else if(deploymentContext != null) {
            }
            //Field hostnameVerifier
            if(baseExecutionContext != null) {
                evaluatedConfiguration.getSslOptions().setHostnameVerifier(
                        evalBooleanProperty("hostnameVerifier", configuration.getSslOptions().isHostnameVerifier(), currentAttributePrefix, baseExecutionContext)
                );
            } else if(deploymentContext != null) {
            }

            //trustStore section begin
            if(evaluatedConfiguration.getSslOptions().getTrustStore() != null) {
                currentAttributePrefix = attributePrefix.concat(".sslOptions.trustStore");
                //Json object based on io.gravitee.plugin.annotation.processor.result.ssl.TrustStoreType
                io.gravitee.plugin.annotation.processor.result.ssl.TrustStoreType type = configuration.getSslOptions().getTrustStore().getType();
                if(baseExecutionContext != null) {
                    type = evalEnumProperty("type", configuration.getSslOptions().getTrustStore().getType(), io.gravitee.plugin.annotation.processor.result.ssl.TrustStoreType.class, currentAttributePrefix, baseExecutionContext);
                }

                switch(type) {
                    case PEM -> {
                        if(type != configuration.getSslOptions().getTrustStore().getType()) {
                            configuration.getSslOptions().setTrustStore(new io.gravitee.plugin.annotation.processor.result.ssl.pem.PEMTrustStore());
                        }
                        //Field io.gravitee.plugin.annotation.processor.result.ssl.pem.PEMTrustStore
                        if(baseExecutionContext != null) {
                            toEval.add(
                                    evalStringProperty("path", ((io.gravitee.plugin.annotation.processor.result.ssl.pem.PEMTrustStore)configuration.getSslOptions().getTrustStore()).getPath(), currentAttributePrefix, baseExecutionContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.pem.PEMTrustStore)evaluatedConfiguration.getSslOptions().getTrustStore()).setPath(value))
                            );
                        } else if(deploymentContext != null) {
                            toEval.add(
                                    evalStringProperty("path", ((io.gravitee.plugin.annotation.processor.result.ssl.pem.PEMTrustStore)configuration.getSslOptions().getTrustStore()).getPath(), currentAttributePrefix, deploymentContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.pem.PEMTrustStore)evaluatedConfiguration.getSslOptions().getTrustStore()).setPath(value)));
                        }
                        if(baseExecutionContext != null) {
                            toEval.add(
                                    evalStringProperty("content", ((io.gravitee.plugin.annotation.processor.result.ssl.pem.PEMTrustStore)configuration.getSslOptions().getTrustStore()).getContent(), currentAttributePrefix, baseExecutionContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.pem.PEMTrustStore)evaluatedConfiguration.getSslOptions().getTrustStore()).setContent(value))
                            );
                        } else if(deploymentContext != null) {
                            toEval.add(
                                    evalStringProperty("content", ((io.gravitee.plugin.annotation.processor.result.ssl.pem.PEMTrustStore)configuration.getSslOptions().getTrustStore()).getContent(), currentAttributePrefix, deploymentContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.pem.PEMTrustStore)evaluatedConfiguration.getSslOptions().getTrustStore()).setContent(value)));
                        }
                    }
                    case PKCS12 -> {
                        if(type != configuration.getSslOptions().getTrustStore().getType()) {
                            configuration.getSslOptions().setTrustStore(new io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12TrustStore());
                        }
                        //Field io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12TrustStore
                        if(baseExecutionContext != null) {
                            toEval.add(
                                    evalStringProperty("path", ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12TrustStore)configuration.getSslOptions().getTrustStore()).getPath(), currentAttributePrefix, baseExecutionContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12TrustStore)evaluatedConfiguration.getSslOptions().getTrustStore()).setPath(value))
                            );
                        } else if(deploymentContext != null) {
                            toEval.add(
                                    evalStringProperty("path", ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12TrustStore)configuration.getSslOptions().getTrustStore()).getPath(), currentAttributePrefix, deploymentContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12TrustStore)evaluatedConfiguration.getSslOptions().getTrustStore()).setPath(value)));
                        }
                        if(baseExecutionContext != null) {
                            toEval.add(
                                    evalStringProperty("content", ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12TrustStore)configuration.getSslOptions().getTrustStore()).getContent(), currentAttributePrefix, baseExecutionContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12TrustStore)evaluatedConfiguration.getSslOptions().getTrustStore()).setContent(value))
                            );
                        } else if(deploymentContext != null) {
                            toEval.add(
                                    evalStringProperty("content", ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12TrustStore)configuration.getSslOptions().getTrustStore()).getContent(), currentAttributePrefix, deploymentContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12TrustStore)evaluatedConfiguration.getSslOptions().getTrustStore()).setContent(value)));
                        }
                        if(baseExecutionContext != null) {
                            toEval.add(
                                    evalStringProperty("password", ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12TrustStore)configuration.getSslOptions().getTrustStore()).getPassword(), currentAttributePrefix, baseExecutionContext, "PASSWORD")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12TrustStore)evaluatedConfiguration.getSslOptions().getTrustStore()).setPassword(value))
                            );
                        } else if(deploymentContext != null) {
                            toEval.add(
                                    evalStringProperty("password", ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12TrustStore)configuration.getSslOptions().getTrustStore()).getPassword(), currentAttributePrefix, deploymentContext, "PASSWORD")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12TrustStore)evaluatedConfiguration.getSslOptions().getTrustStore()).setPassword(value)));
                        }
                        if(baseExecutionContext != null) {
                            toEval.add(
                                    evalStringProperty("alias", ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12TrustStore)configuration.getSslOptions().getTrustStore()).getAlias(), currentAttributePrefix, baseExecutionContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12TrustStore)evaluatedConfiguration.getSslOptions().getTrustStore()).setAlias(value))
                            );
                        } else if(deploymentContext != null) {
                            toEval.add(
                                    evalStringProperty("alias", ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12TrustStore)configuration.getSslOptions().getTrustStore()).getAlias(), currentAttributePrefix, deploymentContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12TrustStore)evaluatedConfiguration.getSslOptions().getTrustStore()).setAlias(value)));
                        }
                    }
                    case JKS -> {
                        if(type != configuration.getSslOptions().getTrustStore().getType()) {
                            configuration.getSslOptions().setTrustStore(new io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSTrustStore());
                        }
                        //Field io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSTrustStore
                        if(baseExecutionContext != null) {
                            toEval.add(
                                    evalStringProperty("path", ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSTrustStore)configuration.getSslOptions().getTrustStore()).getPath(), currentAttributePrefix, baseExecutionContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSTrustStore)evaluatedConfiguration.getSslOptions().getTrustStore()).setPath(value))
                            );
                        } else if(deploymentContext != null) {
                            toEval.add(
                                    evalStringProperty("path", ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSTrustStore)configuration.getSslOptions().getTrustStore()).getPath(), currentAttributePrefix, deploymentContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSTrustStore)evaluatedConfiguration.getSslOptions().getTrustStore()).setPath(value)));
                        }
                        if(baseExecutionContext != null) {
                            toEval.add(
                                    evalStringProperty("content", ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSTrustStore)configuration.getSslOptions().getTrustStore()).getContent(), currentAttributePrefix, baseExecutionContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSTrustStore)evaluatedConfiguration.getSslOptions().getTrustStore()).setContent(value))
                            );
                        } else if(deploymentContext != null) {
                            toEval.add(
                                    evalStringProperty("content", ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSTrustStore)configuration.getSslOptions().getTrustStore()).getContent(), currentAttributePrefix, deploymentContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSTrustStore)evaluatedConfiguration.getSslOptions().getTrustStore()).setContent(value)));
                        }
                        if(baseExecutionContext != null) {
                            toEval.add(
                                    evalStringProperty("password", ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSTrustStore)configuration.getSslOptions().getTrustStore()).getPassword(), currentAttributePrefix, baseExecutionContext, "PASSWORD")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSTrustStore)evaluatedConfiguration.getSslOptions().getTrustStore()).setPassword(value))
                            );
                        } else if(deploymentContext != null) {
                            toEval.add(
                                    evalStringProperty("password", ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSTrustStore)configuration.getSslOptions().getTrustStore()).getPassword(), currentAttributePrefix, deploymentContext, "PASSWORD")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSTrustStore)evaluatedConfiguration.getSslOptions().getTrustStore()).setPassword(value)));
                        }
                        if(baseExecutionContext != null) {
                            toEval.add(
                                    evalStringProperty("alias", ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSTrustStore)configuration.getSslOptions().getTrustStore()).getAlias(), currentAttributePrefix, baseExecutionContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSTrustStore)evaluatedConfiguration.getSslOptions().getTrustStore()).setAlias(value))
                            );
                        } else if(deploymentContext != null) {
                            toEval.add(
                                    evalStringProperty("alias", ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSTrustStore)configuration.getSslOptions().getTrustStore()).getAlias(), currentAttributePrefix, deploymentContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSTrustStore)evaluatedConfiguration.getSslOptions().getTrustStore()).setAlias(value)));
                        }
                    }
                    case NONE -> {
                        if(type != configuration.getSslOptions().getTrustStore().getType()) {
                            configuration.getSslOptions().setTrustStore(new io.gravitee.plugin.annotation.processor.result.ssl.none.NoneTrustStore());
                        }
                        //Field io.gravitee.plugin.annotation.processor.result.ssl.none.NoneTrustStore
                    }
                }
                //Field alias
                if(baseExecutionContext != null) {
                    toEval.add(
                            evalStringProperty("alias", configuration.getSslOptions().getTrustStore().getAlias(), currentAttributePrefix, baseExecutionContext, "")
                                    .doOnSuccess(value -> evaluatedConfiguration.getSslOptions().getTrustStore().setAlias(value))
                    );
                } else if(deploymentContext != null) {
                    toEval.add(
                            evalStringProperty("alias", configuration.getSslOptions().getTrustStore().getAlias(), currentAttributePrefix, deploymentContext, "")
                                    .doOnSuccess(value -> evaluatedConfiguration.getSslOptions().getTrustStore().setAlias(value)));
                }

            }
            //trustStore section end

            //keyStore section begin
            if(evaluatedConfiguration.getSslOptions().getKeyStore() != null) {
                currentAttributePrefix = attributePrefix.concat(".sslOptions.keyStore");
                //Json object based on io.gravitee.plugin.annotation.processor.result.ssl.KeyStoreType
                io.gravitee.plugin.annotation.processor.result.ssl.KeyStoreType type = configuration.getSslOptions().getKeyStore().getType();
                if(baseExecutionContext != null) {
                    type = evalEnumProperty("type", configuration.getSslOptions().getKeyStore().getType(), io.gravitee.plugin.annotation.processor.result.ssl.KeyStoreType.class, currentAttributePrefix, baseExecutionContext);
                }

                switch(type) {
                    case PEM -> {
                        if(type != configuration.getSslOptions().getKeyStore().getType()) {
                            configuration.getSslOptions().setKeyStore(new io.gravitee.plugin.annotation.processor.result.ssl.pem.PEMKeyStore());
                        }
                        //Field io.gravitee.plugin.annotation.processor.result.ssl.pem.PEMKeyStore
                        if(baseExecutionContext != null) {
                            toEval.add(
                                    evalStringProperty("keyPath", ((io.gravitee.plugin.annotation.processor.result.ssl.pem.PEMKeyStore)configuration.getSslOptions().getKeyStore()).getKeyPath(), currentAttributePrefix, baseExecutionContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.pem.PEMKeyStore)evaluatedConfiguration.getSslOptions().getKeyStore()).setKeyPath(value))
                            );
                        } else if(deploymentContext != null) {
                            toEval.add(
                                    evalStringProperty("keyPath", ((io.gravitee.plugin.annotation.processor.result.ssl.pem.PEMKeyStore)configuration.getSslOptions().getKeyStore()).getKeyPath(), currentAttributePrefix, deploymentContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.pem.PEMKeyStore)evaluatedConfiguration.getSslOptions().getKeyStore()).setKeyPath(value)));
                        }
                        if(baseExecutionContext != null) {
                            toEval.add(
                                    evalStringProperty("keyContent", ((io.gravitee.plugin.annotation.processor.result.ssl.pem.PEMKeyStore)configuration.getSslOptions().getKeyStore()).getKeyContent(), currentAttributePrefix, baseExecutionContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.pem.PEMKeyStore)evaluatedConfiguration.getSslOptions().getKeyStore()).setKeyContent(value))
                            );
                        } else if(deploymentContext != null) {
                            toEval.add(
                                    evalStringProperty("keyContent", ((io.gravitee.plugin.annotation.processor.result.ssl.pem.PEMKeyStore)configuration.getSslOptions().getKeyStore()).getKeyContent(), currentAttributePrefix, deploymentContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.pem.PEMKeyStore)evaluatedConfiguration.getSslOptions().getKeyStore()).setKeyContent(value)));
                        }
                        if(baseExecutionContext != null) {
                            toEval.add(
                                    evalStringProperty("certPath", ((io.gravitee.plugin.annotation.processor.result.ssl.pem.PEMKeyStore)configuration.getSslOptions().getKeyStore()).getCertPath(), currentAttributePrefix, baseExecutionContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.pem.PEMKeyStore)evaluatedConfiguration.getSslOptions().getKeyStore()).setCertPath(value))
                            );
                        } else if(deploymentContext != null) {
                            toEval.add(
                                    evalStringProperty("certPath", ((io.gravitee.plugin.annotation.processor.result.ssl.pem.PEMKeyStore)configuration.getSslOptions().getKeyStore()).getCertPath(), currentAttributePrefix, deploymentContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.pem.PEMKeyStore)evaluatedConfiguration.getSslOptions().getKeyStore()).setCertPath(value)));
                        }
                        if(baseExecutionContext != null) {
                            toEval.add(
                                    evalStringProperty("certContent", ((io.gravitee.plugin.annotation.processor.result.ssl.pem.PEMKeyStore)configuration.getSslOptions().getKeyStore()).getCertContent(), currentAttributePrefix, baseExecutionContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.pem.PEMKeyStore)evaluatedConfiguration.getSslOptions().getKeyStore()).setCertContent(value))
                            );
                        } else if(deploymentContext != null) {
                            toEval.add(
                                    evalStringProperty("certContent", ((io.gravitee.plugin.annotation.processor.result.ssl.pem.PEMKeyStore)configuration.getSslOptions().getKeyStore()).getCertContent(), currentAttributePrefix, deploymentContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.pem.PEMKeyStore)evaluatedConfiguration.getSslOptions().getKeyStore()).setCertContent(value)));
                        }
                    }
                    case PKCS12 -> {
                        if(type != configuration.getSslOptions().getKeyStore().getType()) {
                            configuration.getSslOptions().setKeyStore(new io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12KeyStore());
                        }
                        //Field io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12KeyStore
                        if(baseExecutionContext != null) {
                            toEval.add(
                                    evalStringProperty("path", ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12KeyStore)configuration.getSslOptions().getKeyStore()).getPath(), currentAttributePrefix, baseExecutionContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12KeyStore)evaluatedConfiguration.getSslOptions().getKeyStore()).setPath(value))
                            );
                        } else if(deploymentContext != null) {
                            toEval.add(
                                    evalStringProperty("path", ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12KeyStore)configuration.getSslOptions().getKeyStore()).getPath(), currentAttributePrefix, deploymentContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12KeyStore)evaluatedConfiguration.getSslOptions().getKeyStore()).setPath(value)));
                        }
                        if(baseExecutionContext != null) {
                            toEval.add(
                                    evalStringProperty("content", ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12KeyStore)configuration.getSslOptions().getKeyStore()).getContent(), currentAttributePrefix, baseExecutionContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12KeyStore)evaluatedConfiguration.getSslOptions().getKeyStore()).setContent(value))
                            );
                        } else if(deploymentContext != null) {
                            toEval.add(
                                    evalStringProperty("content", ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12KeyStore)configuration.getSslOptions().getKeyStore()).getContent(), currentAttributePrefix, deploymentContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12KeyStore)evaluatedConfiguration.getSslOptions().getKeyStore()).setContent(value)));
                        }
                        if(baseExecutionContext != null) {
                            toEval.add(
                                    evalStringProperty("password", ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12KeyStore)configuration.getSslOptions().getKeyStore()).getPassword(), currentAttributePrefix, baseExecutionContext, "PASSWORD")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12KeyStore)evaluatedConfiguration.getSslOptions().getKeyStore()).setPassword(value))
                            );
                        } else if(deploymentContext != null) {
                            toEval.add(
                                    evalStringProperty("password", ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12KeyStore)configuration.getSslOptions().getKeyStore()).getPassword(), currentAttributePrefix, deploymentContext, "PASSWORD")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12KeyStore)evaluatedConfiguration.getSslOptions().getKeyStore()).setPassword(value)));
                        }
                        if(baseExecutionContext != null) {
                            toEval.add(
                                    evalStringProperty("alias", ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12KeyStore)configuration.getSslOptions().getKeyStore()).getAlias(), currentAttributePrefix, baseExecutionContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12KeyStore)evaluatedConfiguration.getSslOptions().getKeyStore()).setAlias(value))
                            );
                        } else if(deploymentContext != null) {
                            toEval.add(
                                    evalStringProperty("alias", ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12KeyStore)configuration.getSslOptions().getKeyStore()).getAlias(), currentAttributePrefix, deploymentContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12KeyStore)evaluatedConfiguration.getSslOptions().getKeyStore()).setAlias(value)));
                        }
                        if(baseExecutionContext != null) {
                            toEval.add(
                                    evalStringProperty("keyPassword", ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12KeyStore)configuration.getSslOptions().getKeyStore()).getKeyPassword(), currentAttributePrefix, baseExecutionContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12KeyStore)evaluatedConfiguration.getSslOptions().getKeyStore()).setKeyPassword(value))
                            );
                        } else if(deploymentContext != null) {
                            toEval.add(
                                    evalStringProperty("keyPassword", ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12KeyStore)configuration.getSslOptions().getKeyStore()).getKeyPassword(), currentAttributePrefix, deploymentContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.pkcs12.PKCS12KeyStore)evaluatedConfiguration.getSslOptions().getKeyStore()).setKeyPassword(value)));
                        }
                    }
                    case JKS -> {
                        if(type != configuration.getSslOptions().getKeyStore().getType()) {
                            configuration.getSslOptions().setKeyStore(new io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSKeyStore());
                        }
                        //Field io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSKeyStore
                        if(baseExecutionContext != null) {
                            toEval.add(
                                    evalStringProperty("path", ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSKeyStore)configuration.getSslOptions().getKeyStore()).getPath(), currentAttributePrefix, baseExecutionContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSKeyStore)evaluatedConfiguration.getSslOptions().getKeyStore()).setPath(value))
                            );
                        } else if(deploymentContext != null) {
                            toEval.add(
                                    evalStringProperty("path", ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSKeyStore)configuration.getSslOptions().getKeyStore()).getPath(), currentAttributePrefix, deploymentContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSKeyStore)evaluatedConfiguration.getSslOptions().getKeyStore()).setPath(value)));
                        }
                        if(baseExecutionContext != null) {
                            toEval.add(
                                    evalStringProperty("content", ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSKeyStore)configuration.getSslOptions().getKeyStore()).getContent(), currentAttributePrefix, baseExecutionContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSKeyStore)evaluatedConfiguration.getSslOptions().getKeyStore()).setContent(value))
                            );
                        } else if(deploymentContext != null) {
                            toEval.add(
                                    evalStringProperty("content", ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSKeyStore)configuration.getSslOptions().getKeyStore()).getContent(), currentAttributePrefix, deploymentContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSKeyStore)evaluatedConfiguration.getSslOptions().getKeyStore()).setContent(value)));
                        }
                        if(baseExecutionContext != null) {
                            toEval.add(
                                    evalStringProperty("password", ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSKeyStore)configuration.getSslOptions().getKeyStore()).getPassword(), currentAttributePrefix, baseExecutionContext, "PASSWORD")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSKeyStore)evaluatedConfiguration.getSslOptions().getKeyStore()).setPassword(value))
                            );
                        } else if(deploymentContext != null) {
                            toEval.add(
                                    evalStringProperty("password", ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSKeyStore)configuration.getSslOptions().getKeyStore()).getPassword(), currentAttributePrefix, deploymentContext, "PASSWORD")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSKeyStore)evaluatedConfiguration.getSslOptions().getKeyStore()).setPassword(value)));
                        }
                        if(baseExecutionContext != null) {
                            toEval.add(
                                    evalStringProperty("alias", ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSKeyStore)configuration.getSslOptions().getKeyStore()).getAlias(), currentAttributePrefix, baseExecutionContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSKeyStore)evaluatedConfiguration.getSslOptions().getKeyStore()).setAlias(value))
                            );
                        } else if(deploymentContext != null) {
                            toEval.add(
                                    evalStringProperty("alias", ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSKeyStore)configuration.getSslOptions().getKeyStore()).getAlias(), currentAttributePrefix, deploymentContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSKeyStore)evaluatedConfiguration.getSslOptions().getKeyStore()).setAlias(value)));
                        }
                        if(baseExecutionContext != null) {
                            toEval.add(
                                    evalStringProperty("keyPassword", ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSKeyStore)configuration.getSslOptions().getKeyStore()).getKeyPassword(), currentAttributePrefix, baseExecutionContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSKeyStore)evaluatedConfiguration.getSslOptions().getKeyStore()).setKeyPassword(value))
                            );
                        } else if(deploymentContext != null) {
                            toEval.add(
                                    evalStringProperty("keyPassword", ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSKeyStore)configuration.getSslOptions().getKeyStore()).getKeyPassword(), currentAttributePrefix, deploymentContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.ssl.jks.JKSKeyStore)evaluatedConfiguration.getSslOptions().getKeyStore()).setKeyPassword(value)));
                        }
                    }
                    case NONE -> {
                        if(type != configuration.getSslOptions().getKeyStore().getType()) {
                            configuration.getSslOptions().setKeyStore(new io.gravitee.plugin.annotation.processor.result.ssl.none.NoneKeyStore());
                        }
                        //Field io.gravitee.plugin.annotation.processor.result.ssl.none.NoneKeyStore
                    }
                }

            }
            //keyStore section end

        }
        //sslOptions section end

        //saslMechanism section begin
        if(evaluatedConfiguration.getSaslMechanism() != null) {
            currentAttributePrefix = attributePrefix.concat(".saslMechanism");
//Json object based on io.gravitee.plugin.annotation.processor.result.sasl.SaslMechanismType
            io.gravitee.plugin.annotation.processor.result.sasl.SaslMechanismType type = configuration.getSaslMechanism().getType();
            if(baseExecutionContext != null) {
                type = evalEnumProperty("type", configuration.getSaslMechanism().getType(), io.gravitee.plugin.annotation.processor.result.sasl.SaslMechanismType.class, currentAttributePrefix, baseExecutionContext);
            }

            switch(type) {
                // Start Case NONE
                case NONE -> {
                    if(type != configuration.getSaslMechanism().getType()) {
                        configuration.setSaslMechanism(new io.gravitee.plugin.annotation.processor.result.sasl.none.NoneSaslMechanism());
                    }

                }
                // End Case NONE

                // Start Case AWS_MSK_IAM
                case AWS_MSK_IAM -> {
                    if(type != configuration.getSaslMechanism().getType()) {
                        configuration.setSaslMechanism(new io.gravitee.plugin.annotation.processor.result.sasl.awsmskiam.AwsMskIamSaslMechanism());
                    }

                    //config section begin
                    if(((io.gravitee.plugin.annotation.processor.result.sasl.awsmskiam.AwsMskIamSaslMechanism)evaluatedConfiguration.getSaslMechanism()).getConfig() != null) {
                        currentAttributePrefix = attributePrefix.concat(".saslMechanism.config");
                        //Field saslJaasConfig
                        if(baseExecutionContext != null) {
                            toEval.add(
                                    evalStringProperty("saslJaasConfig", ((io.gravitee.plugin.annotation.processor.result.sasl.awsmskiam.AwsMskIamSaslMechanism)configuration.getSaslMechanism()).getConfig().getSaslJaasConfig(), currentAttributePrefix, baseExecutionContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.sasl.awsmskiam.AwsMskIamSaslMechanism)evaluatedConfiguration.getSaslMechanism()).getConfig().setSaslJaasConfig(value))
                            );
                        } else if(deploymentContext != null) {
                            toEval.add(
                                    evalStringProperty("saslJaasConfig", ((io.gravitee.plugin.annotation.processor.result.sasl.awsmskiam.AwsMskIamSaslMechanism)configuration.getSaslMechanism()).getConfig().getSaslJaasConfig(), currentAttributePrefix, deploymentContext, "")
                                            .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.sasl.awsmskiam.AwsMskIamSaslMechanism)evaluatedConfiguration.getSaslMechanism()).getConfig().setSaslJaasConfig(value)));
                        }

                    }
                    //config section end

                }
                // End Case AWS_MSK_IAM

                // Start Case SCRAM_SHA_256
                case SCRAM_SHA_256 -> {
                    if(type != configuration.getSaslMechanism().getType()) {
                        configuration.setSaslMechanism(new io.gravitee.plugin.annotation.processor.result.sasl.scramsha256.ScramSha256SaslMechanism());
                    }
                    //Field username
                    if(baseExecutionContext != null) {
                        toEval.add(
                                evalStringProperty("username", ((io.gravitee.plugin.annotation.processor.result.sasl.scramsha256.ScramSha256SaslMechanism)configuration.getSaslMechanism()).getUsername(), currentAttributePrefix, baseExecutionContext, "GENERIC")
                                        .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.sasl.scramsha256.ScramSha256SaslMechanism)evaluatedConfiguration.getSaslMechanism()).setUsername(value))
                        );
                    } else if(deploymentContext != null) {
                        toEval.add(
                                evalStringProperty("username", ((io.gravitee.plugin.annotation.processor.result.sasl.scramsha256.ScramSha256SaslMechanism)configuration.getSaslMechanism()).getUsername(), currentAttributePrefix, deploymentContext, "GENERIC")
                                        .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.sasl.scramsha256.ScramSha256SaslMechanism)evaluatedConfiguration.getSaslMechanism()).setUsername(value)));
                    }
                    //Field password
                    if(baseExecutionContext != null) {
                        toEval.add(
                                evalStringProperty("password", ((io.gravitee.plugin.annotation.processor.result.sasl.scramsha256.ScramSha256SaslMechanism)configuration.getSaslMechanism()).getPassword(), currentAttributePrefix, baseExecutionContext, "PASSWORD")
                                        .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.sasl.scramsha256.ScramSha256SaslMechanism)evaluatedConfiguration.getSaslMechanism()).setPassword(value))
                        );
                    } else if(deploymentContext != null) {
                        toEval.add(
                                evalStringProperty("password", ((io.gravitee.plugin.annotation.processor.result.sasl.scramsha256.ScramSha256SaslMechanism)configuration.getSaslMechanism()).getPassword(), currentAttributePrefix, deploymentContext, "PASSWORD")
                                        .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.sasl.scramsha256.ScramSha256SaslMechanism)evaluatedConfiguration.getSaslMechanism()).setPassword(value)));
                    }
                    //Field customJassConfigMap
                    if(baseExecutionContext != null) {
                        toEvalMap.add(
                                evalMapStringProperty("customJassConfigMap", ((io.gravitee.plugin.annotation.processor.result.sasl.scramsha256.ScramSha256SaslMechanism)configuration.getSaslMechanism()).getCustomJassConfigMap(), currentAttributePrefix, baseExecutionContext)
                                        .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.sasl.scramsha256.ScramSha256SaslMechanism)evaluatedConfiguration.getSaslMechanism()).setCustomJassConfigMap(value))
                        );
                    } else if(deploymentContext != null) {
                        toEvalMap.add(
                                evalMapStringProperty("customJassConfigMap", ((io.gravitee.plugin.annotation.processor.result.sasl.scramsha256.ScramSha256SaslMechanism)configuration.getSaslMechanism()).getCustomJassConfigMap(), currentAttributePrefix, deploymentContext)
                                        .doOnSuccess(value -> ((io.gravitee.plugin.annotation.processor.result.sasl.scramsha256.ScramSha256SaslMechanism)evaluatedConfiguration.getSaslMechanism()).setCustomJassConfigMap(value)));
                    }

                }
                // End Case SCRAM_SHA_256

            }
// End Json object based on io.gravitee.plugin.annotation.processor.result.sasl.SaslMechanismType

        }
        //saslMechanism section end

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
        Completable toEvalCompletable = Flowable.fromIterable(toEval).concatMapMaybe(m -> m).ignoreElements();
        Completable toEvalListCompletable = Flowable.fromIterable(toEvalList).concatMapMaybe(m -> m).ignoreElements();
        Completable toEvalHeaderListCompletable = Flowable.fromIterable(toEvalHeaderList).concatMapMaybe(m -> m).ignoreElements();
        Completable toEvalMapCompletable = Flowable.fromIterable(toEvalMap).concatMapMaybe(m -> m).ignoreElements();

        return Completable.concatArray(toEvalCompletable, toEvalListCompletable, toEvalHeaderListCompletable, toEvalMapCompletable)
            .andThen(Completable.fromRunnable(() -> validateConfiguration(evaluatedConfiguration)))
            .andThen(Completable.fromRunnable(() -> {
                if(baseExecutionContext != null) {
                    baseExecutionContext.setInternalAttribute("testConfiguration-"+this.internalId, evaluatedConfiguration);
                }
            }))
            .onErrorResumeNext(t -> {
                if(baseExecutionContext != null && baseExecutionContext instanceof HttpPlainExecutionContext httpPlainExecutionContext) {
                    return httpPlainExecutionContext.interruptWith(new ExecutionFailure(500).message("Invalid configuration").key(FAILURE_CONFIGURATION_INVALID));
                }
                return Completable.error(t);
            })
            .toSingle(() -> evaluatedConfiguration);
    }
}
