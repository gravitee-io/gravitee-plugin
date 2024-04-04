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
package io.gravitee.plugin.core.spring;

import io.gravitee.common.event.EventManager;
import io.gravitee.plugin.core.api.PluginContextFactory;
import io.gravitee.plugin.core.api.PluginHandler;
import io.gravitee.plugin.core.internal.PluginContextFactoryImpl;
import io.gravitee.plugin.core.internal.PluginEventListener;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@Configuration
public class PluginConfiguration {

    @Bean
    public PluginContextFactory pluginContextFactory() {
        return new PluginContextFactoryImpl();
    }

    @Bean
    public PluginEventListener pluginEventListener(
        Collection<PluginHandler> pluginHandlers,
        @Qualifier("pluginEventManager") EventManager eventManager,
        Environment environment
    ) {
        return new PluginEventListener(pluginHandlers, eventManager, environment);
    }
}
