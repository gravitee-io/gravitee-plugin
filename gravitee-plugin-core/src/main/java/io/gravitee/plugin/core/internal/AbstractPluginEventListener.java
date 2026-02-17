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

import io.gravitee.common.event.EventListener;
import io.gravitee.common.event.EventManager;
import io.gravitee.common.service.AbstractService;
import io.gravitee.plugin.core.api.Plugin;
import io.gravitee.plugin.core.api.PluginEvent;
import io.gravitee.plugin.core.api.PluginHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractPluginEventListener
    extends AbstractService<AbstractPluginEventListener>
    implements EventListener<PluginEvent, Plugin> {

    public static final String SECRET_PROVIDER = "secret-provider";
    public static final String DATA_PLANE = "data-plane";
    /**
     * Allows to define priority between the different plugin types.
     */
    private static final List<String> pluginPriority = Arrays.asList(
        SECRET_PROVIDER,
        "cluster",
        "cache",
        "repository",
        DATA_PLANE,
        "alert",
        "cockpit"
    );

    private final Collection<PluginHandler> pluginHandlers;

    private final EventManager eventManager;

    private final Environment environment;

    @Getter(AccessLevel.PACKAGE)
    private final Map<PluginKey, Plugin> plugins = new ConcurrentHashMap<>();

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        eventManager.subscribeForEvents(this, PluginEvent.class);
    }

    protected void addPlugin(Plugin plugin) {
        PluginKey pluginKey = new PluginKey(plugin.id(), plugin.type());

        if (plugins.containsKey(pluginKey)) {
            Plugin installed = plugins.get(pluginKey);
            log.warn("Plugin '{}' [{}] is already loaded [{}]", plugin.id(), plugin.manifest().version(), installed.manifest().version());
        } else {
            plugins.put(pluginKey, plugin);
        }
    }

    protected void deployPlugins() {
        Map<String, List<Plugin>> resolvedDependencies = new HashMap<>();

        final List<Plugin> sortedByPriority = this.plugins.values()
            .stream()
            .sorted(
                Comparator.<Plugin>comparingInt(o -> o.manifest().priority()).thenComparing(
                    new PluginComparator(environment.getProperty("secrets.loadFirst"))
                )
            )
            .toList();

        plugins
            .values()
            .forEach(p ->
                plugins
                    .values()
                    .forEach(other -> {
                        if (
                            p
                                .manifest()
                                .dependencies()
                                .stream()
                                .anyMatch(d -> d.matches(other))
                        ) {
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

    private record PluginKey(String id, String type) {}

    protected static class PluginComparator implements Comparator<Plugin> {

        private final String loadFirst;

        public PluginComparator(String loadFirst) {
            this.loadFirst = loadFirst;
        }

        @Override
        public int compare(Plugin o1, Plugin o2) {
            if (loadFirst != null && o1.manifest().type().equalsIgnoreCase(SECRET_PROVIDER)) {
                if (o1.manifest().id().equals(loadFirst)) {
                    return -1;
                } else if (o2.manifest().id().equals(loadFirst)) {
                    return 1;
                }
            }

            int pos1 = pluginPriority.indexOf(o1.type());
            int pos2 = pluginPriority.indexOf(o2.type());

            if (pos1 >= 0) {
                if (pos2 >= 0) {
                    // The plugin are both defined in the priority list. Respect the order defined.
                    return Integer.compare(pos1, pos2);
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
