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
package io.gravitee.plugin.policy.internal;

import static org.reflections.ReflectionUtils.withAnnotation;
import static org.reflections.ReflectionUtils.withModifier;

import io.gravitee.policy.api.annotations.OnRequest;
import io.gravitee.policy.api.annotations.OnRequestContent;
import io.gravitee.policy.api.annotations.OnResponse;
import io.gravitee.policy.api.annotations.OnResponseContent;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.reflections.ReflectionUtils;

/**
 * @author David BRASSELY (brasseld at gmail.com)
 * @author GraviteeSource Team
 */
public class PolicyMethodResolver {

    private static final Class<? extends Annotation>[] RESOLVABLE_ANNOTATIONS = new Class[] {
        OnRequest.class,
        OnResponse.class,
        OnRequestContent.class,
        OnResponseContent.class,
    };

    public Map<Class<? extends Annotation>, Method> resolve(Class<?> policyClass) {
        Map<Class<? extends Annotation>, Method> methods = new HashMap<>();

        for (Class<? extends Annotation> annot : RESOLVABLE_ANNOTATIONS) {
            Set<Method> resolved = ReflectionUtils.getAllMethods(policyClass, withModifier(Modifier.PUBLIC), withAnnotation(annot));

            if (!resolved.isEmpty()) {
                methods.put(annot, resolved.iterator().next());
            }
        }

        return methods;
    }
}
