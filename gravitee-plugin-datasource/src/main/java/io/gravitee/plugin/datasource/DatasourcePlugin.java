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
import io.gravitee.datasource.api.DatasourceConfiguration;
import io.gravitee.datasource.api.DatasourceConfigurationMapper;
import io.gravitee.plugin.core.api.ConfigurablePlugin;
import io.gravitee.plugin.core.api.Plugin;

public interface DatasourcePlugin<C extends DatasourceConfiguration, CM extends DatasourceConfigurationMapper>
    extends ConfigurablePlugin<C> {
    String PLUGIN_TYPE = "datasource";

    Plugin pluginDefinition();

    Class<? extends Datasource> datasource();

    Class<CM> configurationMapper();

    @Override
    default String type() {
        return PLUGIN_TYPE;
    }
}
