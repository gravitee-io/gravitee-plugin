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

import io.gravitee.datasource.api.DatasourceConfiguration;
import io.gravitee.datasource.api.DatasourceConfigurationMapper;
import io.gravitee.plugin.core.api.Plugin;
import io.gravitee.plugin.core.api.PluginManifest;
import java.net.URL;
import java.nio.file.Path;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class DatasourcePluginImpl implements DatasourcePlugin {

    private final Plugin plugin;
    private final Class<?> datasourceClass;
    private Class<? extends DatasourceConfiguration> configuration;
    private Class<? extends DatasourceConfigurationMapper> configurationMapper;

    public DatasourcePluginImpl(final Plugin plugin, final Class<?> datasourceClass) {
        this.plugin = plugin;
        this.datasourceClass = datasourceClass;
    }

    @Override
    public Class<?> datasource() {
        return datasourceClass;
    }

    @Override
    public Plugin pluginDefinition() {
        return plugin;
    }

    @Override
    public String clazz() {
        return plugin.clazz();
    }

    @Override
    public URL[] dependencies() {
        return plugin.dependencies();
    }

    @Override
    public String id() {
        return plugin.id();
    }

    @Override
    public PluginManifest manifest() {
        return plugin.manifest();
    }

    @Override
    public Path path() {
        return plugin.path();
    }

    @Override
    public boolean deployed() {
        return plugin.deployed();
    }

    @Override
    public Class configuration() {
        return configuration;
    }

    public void setConfiguration(Class<? extends DatasourceConfiguration> datasourceConfigurationClass) {
        this.configuration = datasourceConfigurationClass;
    }

    @Override
    public Class configurationMapper() {
        return configurationMapper;
    }

    public void setConfigurationMapper(Class<? extends DatasourceConfigurationMapper> datasourceConfigurationMapperClass) {
        this.configurationMapper = datasourceConfigurationMapperClass;
    }
}
