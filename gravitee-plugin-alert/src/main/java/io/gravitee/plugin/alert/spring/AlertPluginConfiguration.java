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
package io.gravitee.plugin.alert.spring;

import io.gravitee.alert.api.trigger.TriggerProvider;
import io.gravitee.plugin.alert.AlertClassLoaderFactory;
import io.gravitee.plugin.alert.AlertEventProducer;
import io.gravitee.plugin.alert.AlertEventProducerManager;
import io.gravitee.plugin.alert.AlertTriggerProviderManager;
import io.gravitee.plugin.alert.internal.*;
import org.springframework.context.annotation.Bean;

/**
 * @author Azize ELAMRANI (azize.elamrani at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AlertPluginConfiguration {

    @Bean
    public AlertEventProducerManager alertEventProducerManager() {
        return new AlertEventProducerManagerImpl();
    }

    @Bean
    public AlertTriggerProviderManager alertTriggerProviderManager() {
        return new AlertTriggerProviderManagerImpl();
    }

    @Bean
    public AlertEventProducer alertEventProducer() {
        return new AlertEventProducerImpl();
    }

    @Bean
    public TriggerProvider alertTriggerProvider() {
        return new AlertTriggerProviderImpl();
    }

    @Bean
    public AlertClassLoaderFactory alertClassLoaderFactory() {
        return new AlertClassLoaderFactoryImpl();
    }
}