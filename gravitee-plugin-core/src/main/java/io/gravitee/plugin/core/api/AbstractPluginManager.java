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
package io.gravitee.plugin.core.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public abstract class AbstractPluginManager<T extends Plugin> implements PluginManager<T> {

    private final Map<String, T> plugins = new HashMap<>();

    @Override
    public void register(T plugin) {
        plugins.putIfAbsent(plugin.id(), plugin);
    }

    @Override
    public Collection<T> findAll() {
        return plugins.values().stream().filter(Plugin::deployed).collect(Collectors.toList());
    }

    @Override
    public Collection<T> findAll(boolean includeNotDeployed) {
        if (includeNotDeployed) {
            return plugins.values();
        }
        return findAll();
    }

    @Override
    public T get(String pluginId) {
        T plugin = plugins.get(pluginId);
        if (plugin != null && !plugin.deployed()) {
            return null;
        }
        return plugin;
    }

    @Override
    public T get(String pluginId, boolean includeNotDeployed) {
        if (includeNotDeployed) {
            return plugins.get(pluginId);
        }
        return get(pluginId);
    }
}
