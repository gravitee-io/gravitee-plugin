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
package io.gravitee.plugin.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Remi Baptiste (remi.baptiste at graviteesource.com)
 * @author GraviteeSource Team
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface ConfigurationEvaluator {
    /**
     * <p>A string that represent the prefix the evaluator will use to determine
     * if an attribute exist in the context to override a property.
     * </p>
     * <p>
     * If set the prefix must follow the defined convention: gravitee.attributes.[type].[id]
     * where type is the type of plugin (ie: "endpoint") and id is the id of the plugin (ie : “kafka”).
     * </p>
     * By default it is "" as it is not mandatory when the evaluation context is {@link io.gravitee.gateway.reactive.api.context.DeploymentContext}
     */
    String attributePrefix() default "";
}
