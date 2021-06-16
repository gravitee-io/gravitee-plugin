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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class PluginEventListener extends AbstractService implements EventListener<PluginEvent, Plugin> {

    private final static Logger LOGGER = LoggerFactory.getLogger(PluginEventListener.class);

    /**
     * Allows to define priority between the different plugin types.
     */
    private final static List<String> pluginPriority = Arrays.asList("repository", "alert", "cockpit");

    @Value("${plugins.failOnDuplicate:true}")
    private boolean failOnDuplicate;

    @Autowired
    private Collection<PluginHandler> pluginHandlers;

    @Autowired
    private EventManager eventManager;

    private final Map<PluginKey, Plugin> plugins = new HashMap<>();

    @Override
    public void onEvent(Event<PluginEvent, Plugin> event) {
        switch (event.type()) {
            case DEPLOYED:
                LOGGER.debug("Receive an event for plugin {} [{}]", event.content().id(), event.type());
                addPlugin(event.content());
                break;
            case ENDED:
                LOGGER.info("All plugins have been loaded. Installing...");
                deployPlugins();
                break;
        }
    }

    private void addPlugin(Plugin plugin) {
        PluginKey pluginKey = new PluginKey(plugin.id(), plugin.type());

        if (plugins.containsKey(pluginKey)) {
            Plugin installed = plugins.get(pluginKey);
            if (failOnDuplicate) {
                throw new IllegalStateException(
                        String.format("Plugin '%s' [%s] is already loaded [%s]", plugin.id(),
                                plugin.manifest().version(), installed.manifest().version()));
            } else {
                LOGGER.warn("Plugin '{}' [{}] is already loaded [{}]", plugin.id(),
                        plugin.manifest().version(), installed.manifest().version());
            }
        } else {
            plugins.put(pluginKey, plugin);
        }
    }

    private void deployPlugins() {
        // Plugins loading should be re-ordered to manage inter-dependencies
        Stream
                .concat(
                        Stream.of("repository"),
                        plugins.values().stream().map(Plugin::type).distinct())
                .distinct()
                .sorted(new PluginTypeComparator())
                .forEach(this::deployPlugins);
    }

    private void deployPlugins(String pluginType) {
        LOGGER.info("Installing {} plugins...", pluginType);
        plugins.values().stream()
                .filter(plugin -> pluginType.equalsIgnoreCase(plugin.type()))
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

    public void setFailOnDuplicate(boolean failOnDuplicate) {
        this.failOnDuplicate = failOnDuplicate;
    }

    private static class PluginKey {
        private final String id;
        private final String type;

        public PluginKey(final String id, final String type) {
            this.id = id;
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PluginKey pluginKey = (PluginKey) o;

            if (!id.equals(pluginKey.id)) return false;
            return type == pluginKey.type;
        }

        @Override
        public int hashCode() {
            int result = id.hashCode();
            result = 31 * result + type.hashCode();
            return result;
        }
    }

    private static class PluginTypeComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            Integer pos1 = pluginPriority.indexOf(o1);
            Integer pos2 = pluginPriority.indexOf(o2);

            if (pos1 >= 0) {
                if (pos2 >= 0) {
                    // The plugin are both defined in the priority list. Respect the order defined.
                    return pos1.compareTo(pos2);
                }

                // The second plugin is not defined in the priority list, plugin 1 takes precedence.
                return -1;
            }

            if (pos2 >= 0) {
                // First plugin is not defined in the priority list, plugin 2 takes precedence.
                return 1;
            }

            // Both plugins are not in the priority list, keep the order unchanged.
            return 0;

        }
    }
}
