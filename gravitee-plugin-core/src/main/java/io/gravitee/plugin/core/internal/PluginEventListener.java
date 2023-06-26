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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PluginEventListener extends AbstractService<PluginEventListener> implements EventListener<PluginEvent, Plugin> {

    /**
     * Allows to define priority between the different plugin types.
     */
    private static final List<String> pluginPriority = Arrays.asList("cluster", "cache", "repository", "alert", "cockpit");

    private final Collection<PluginHandler> pluginHandlers;

    private final EventManager eventManager;

    @Getter(AccessLevel.PACKAGE)
    private final Map<PluginKey, Plugin> plugins = new ConcurrentHashMap<>();

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

    private void addPlugin(Plugin plugin) {
        PluginKey pluginKey = new PluginKey(plugin.id(), plugin.type());

        if (plugins.containsKey(pluginKey)) {
            Plugin installed = plugins.get(pluginKey);
            log.warn("Plugin '{}' [{}] is already loaded [{}]", plugin.id(), plugin.manifest().version(), installed.manifest().version());
        } else {
            plugins.put(pluginKey, plugin);
        }
    }

    private void deployPlugins() {
        Map<String, List<Plugin>> resolvedDependencies = new HashMap<>();

        final List<Plugin> sortedByPriority =
            this.plugins.values()
                .stream()
                .sorted(Comparator.<Plugin>comparingInt(o -> o.manifest().priority()).thenComparing(new PluginComparator()))
                .collect(Collectors.toList());

        plugins
            .values()
            .forEach(p ->
                plugins
                    .values()
                    .forEach(other -> {
                        if (p.manifest().dependencies().stream().anyMatch(d -> d.matches(other))) {
                            resolvedDependencies.computeIfAbsent(p.type() + p.id(), s -> new ArrayList<>()).add(other);
                        }
                    })
            );

        List<Plugin> deployedPlugins = new ArrayList<>(this.plugins.size());

        sortedByPriority.forEach(plugin -> deployPlugin(plugin, resolvedDependencies, deployedPlugins));
    }

    private void deployPlugin(Plugin plugin, Map<String, List<Plugin>> resolvedDependencies, List<Plugin> deployedPlugins) {
        if (deployedPlugins.contains(plugin)) {
            return;
        }

        // Deploy all plugins the plugin depends on.
        resolvedDependencies
            .getOrDefault(plugin.type() + plugin.id(), Collections.emptyList())
            .forEach(dependencyPlugin -> deployPlugin(dependencyPlugin, resolvedDependencies, deployedPlugins));

        log.debug("Installing {} plugins...", plugin.id());
        pluginHandlers
            .stream()
            .filter(pluginHandler -> pluginHandler.canHandle(plugin))
            .forEach(pluginHandler -> {
                log.debug("Plugin {} has been managed by {}", plugin.id(), pluginHandler.getClass());
                pluginHandler.handle(plugin);
            });

        deployedPlugins.add(plugin);
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        eventManager.subscribeForEvents(this, PluginEvent.class);
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

    private static class PluginComparator implements Comparator<Plugin> {

        @Override
        public int compare(Plugin o1, Plugin o2) {
            Integer pos1 = pluginPriority.indexOf(o1.type());
            Integer pos2 = pluginPriority.indexOf(o2.type());

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
