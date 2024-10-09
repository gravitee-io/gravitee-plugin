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

import static org.springframework.util.StringUtils.hasText;

import io.gravitee.datasource.api.Datasource;
import io.gravitee.datasource.api.DatasourceConfiguration;
import io.gravitee.plugin.core.api.AbstractPluginHandler;
import io.gravitee.plugin.core.api.Plugin;
import io.gravitee.plugin.core.api.PluginContextFactory;
import io.gravitee.plugin.datasource.internal.DatasourceConfigurationClassFinder;
import io.gravitee.plugin.datasource.internal.DatasourceConfigurationFactory;
import io.gravitee.plugin.datasource.internal.DatasourceConfigurationMapperClassFinder;
import io.gravitee.plugin.datasource.spring.DatasourcePluginConfiguration;
import java.net.URLClassLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Import(DatasourcePluginConfiguration.class)
public class DatasourcePluginHandler extends AbstractPluginHandler {

    private static final String CONF_DATASOURCES = "datasources";
    public static final String DATASOURCE_ID = "id";
    public static final String DATASOURCE_ENABLED = "enabled";
    public static final String DATASOURCE_SETTINGS = "settings";

    @Autowired
    private DatasourcePluginManager datasourcePluginManager;

    @Autowired
    private Environment environment;

    @Autowired
    private PluginContextFactory pluginContextFactory;

    @Autowired
    private DatasourceConfigurationFactory configurationFactory;

    @Override
    protected String type() {
        return DatasourcePlugin.PLUGIN_TYPE;
    }

    @Override
    protected ClassLoader getClassLoader(Plugin plugin) throws Exception {
        return new URLClassLoader(plugin.dependencies(), this.getClass().getClassLoader());
    }

    @Override
    protected void handle(Plugin plugin, Class<?> pluginClass) {
        // register plugin definition to make DS available
        // through UI not only through gravitee.yaml
        DatasourcePlugin datasourcePlugin = create(plugin, pluginClass);
        register(datasourcePlugin);

        // instantiate plugin form gravitee.yaml
        if (plugin.deployed()) {
            var pluginIndex = 0;
            while (environment.containsProperty(datasourceConfigEntries(plugin, pluginIndex, DATASOURCE_ID))) {
                instantiate(datasourcePlugin, pluginIndex++);
            }
        }
    }

    private void instantiate(DatasourcePlugin datasourcePlugin, int pluginIndex) {
        final var plugin = datasourcePlugin.pluginDefinition();
        if (environment.getProperty(datasourceConfigEntries(plugin, pluginIndex, DATASOURCE_ENABLED), boolean.class, true)) {
            final var instanceName = environment.getProperty(datasourceConfigEntries(plugin, pluginIndex, DATASOURCE_ID), String.class);
            if (!hasText(instanceName)) {
                logger.warn("name is missing for datasource plugin {}, skip the instance creation", plugin.id());
                return;
            }

            logger.info("Loading datasource plugin {} with name {}", plugin.id(), instanceName);
            try {
                DatasourceConfiguration configuration = configurationFactory.build(
                    datasourcePlugin.configurationMapper(),
                    datasourceConfigEntries(plugin, pluginIndex, DATASOURCE_SETTINGS)
                );
                final var ds = createInstance(datasourcePlugin, configuration);
                ds.start();

                this.datasourcePluginManager.addDatasource(plugin, instanceName, ds);
            } catch (Exception e) {
                logger.error("Unable to instantiate datasource plugin of type {}", plugin.type(), e);
            }
        }
    }

    private static String datasourceConfigEntries(Plugin plugin, int pluginIndex, String key) {
        return CONF_DATASOURCES + "." + plugin.id() + "[" + pluginIndex + "]." + key;
    }

    public Datasource createInstance(DatasourcePlugin datasourcePlugin, DatasourceConfiguration config) {
        var pluginContext = pluginContextFactory.create(datasourcePlugin);
        var ds = (Datasource) pluginContext.getAutowireCapableBeanFactory().createBean(datasourcePlugin.datasource());
        ds.setConfiguration(config);
        return ds;
    }

    protected DatasourcePlugin create(Plugin plugin, Class<?> pluginClass) {
        DatasourcePluginImpl resourcePlugin = new DatasourcePluginImpl(plugin, pluginClass);
        resourcePlugin.setConfiguration(new DatasourceConfigurationClassFinder().lookupFirst(pluginClass));
        resourcePlugin.setConfigurationMapper(new DatasourceConfigurationMapperClassFinder().lookupFirst(pluginClass));
        return resourcePlugin;
    }

    protected void register(DatasourcePlugin plugin) {
        datasourcePluginManager.register(plugin);
    }

    @Override
    public boolean canHandle(Plugin plugin) {
        return type().equalsIgnoreCase(plugin.type());
    }
}
