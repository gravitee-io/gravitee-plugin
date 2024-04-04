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
package io.gravitee.plugin.core.internal;

import io.gravitee.common.event.Event;
import io.gravitee.common.event.EventManager;
import io.gravitee.plugin.core.api.Plugin;
import io.gravitee.plugin.core.api.PluginEvent;
import io.gravitee.plugin.core.api.PluginHandler;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@Slf4j
public class PluginEventListener extends AbstractPluginEventListener {

    public PluginEventListener(Collection<PluginHandler> pluginHandlers, EventManager eventManager, Environment environment) {
        super(pluginHandlers, eventManager, environment);
    }

    @Override
    public void onEvent(Event<PluginEvent, Plugin> event) {
        switch (event.type()) {
            case DEPLOYED:
                log.debug("Receive an event for plugin {} [{}]", event.content().id(), event.type());
                addPlugin(event.content());
                break;
            case ENDED:
                log.info("All plugins have been loaded. Installing...");
                deployPlugins();
                break;
            case UNDEPLOYED:
                // no op
                break;
        }
    }
}
