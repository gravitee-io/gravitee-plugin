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
package io.gravitee.plugin.core.internal;

import io.gravitee.common.event.Event;
import io.gravitee.common.event.EventListener;
import io.gravitee.common.event.EventManager;
import io.gravitee.common.service.AbstractService;
import io.gravitee.plugin.core.api.Plugin;
import io.gravitee.plugin.core.api.PluginEvent;
import io.gravitee.plugin.core.api.PluginHandler;
import io.gravitee.plugin.core.api.PluginType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author David BRASSELY (brasseld at gmail.com)
 */
public class PluginEventListener extends AbstractService implements EventListener<PluginEvent, Plugin> {

    private final static Logger LOGGER = LoggerFactory.getLogger(PluginEventListener.class);

    @Autowired
    private Collection<PluginHandler> pluginHandlers;

    @Autowired
    private EventManager eventManager;

    private final List<Plugin> plugins = new ArrayList<>();

    @Override
    public void onEvent(Event<PluginEvent, Plugin> event) {
        switch (event.type()) {
            case DEPLOYED:
                LOGGER.debug("Receive an event for plugin {} [{}]", event.content().id(), event.type());
                plugins.add(event.content());
                break;
            case ENDED:
                LOGGER.info("All plugins have been loaded. Installing...");
                deployPlugins();
                break;
        }
    }

    private void deployPlugins() {
        // Plugins loading should be re-ordered to manage inter-dependencies
        deployPlugins(PluginType.REPOSITORY);
        deployPlugins(PluginType.POLICY);
        deployPlugins(PluginType.SERVICE);
        deployPlugins(PluginType.REPORTER);
    }

    private void deployPlugins(PluginType pluginType) {
        plugins.stream()
                .filter(plugin -> pluginType == plugin.type())
                .forEach(plugin ->
                        pluginHandlers.stream()
                                .filter(pluginHandler -> pluginHandler.canHandle(plugin))
                                .forEach(pluginHandler -> {
                                    LOGGER.debug("Plugin {} has been managed by {}", plugin.id(), pluginHandler.getClass());
                                    pluginHandler.handle(plugin);
                                }));
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        eventManager.subscribeForEvents(this, PluginEvent.class);
    }
}
