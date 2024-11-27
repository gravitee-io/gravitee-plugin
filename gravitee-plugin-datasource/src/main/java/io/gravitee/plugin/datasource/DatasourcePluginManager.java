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
package io.gravitee.plugin.datasource;

import io.gravitee.datasource.api.Datasource;
import io.gravitee.plugin.core.api.AbstractConfigurablePluginManager;
import io.gravitee.plugin.core.api.Plugin;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class DatasourcePluginManager extends AbstractConfigurablePluginManager<DatasourcePlugin> {

    private Map<String, Map<String, Datasource>> datasources = new ConcurrentHashMap<>();

    public void addDatasource(Plugin plugin, String instanceName, Datasource datasource) {
        this.datasources.putIfAbsent(plugin.id(), new ConcurrentHashMap<>());
        this.datasources.get(plugin.id()).putIfAbsent(instanceName, datasource);
    }

    public Optional<Datasource> lookup(String pluginId, String instanceName) {
        return Optional.ofNullable(this.datasources.get(pluginId)).map(instances -> instances.get(instanceName));
    }
}
