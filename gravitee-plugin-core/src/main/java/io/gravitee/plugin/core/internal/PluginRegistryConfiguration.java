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

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class PluginRegistryConfiguration implements InitializingBean {

    private static final String PLUGIN_WORK_DIR_PROPERTY = "plugins.workDir";

    private static final String PLUGIN_PATH_PROPERTY = "plugins.path[%s]";

    @Value("${plugins.path:${gravitee.home}/plugins}")
    private String defaultPluginPath;

    private String[] pluginsPath;

    @Autowired
    private Environment environment;

    private String pluginWorkDir;

    public void afterPropertiesSet() {
        String key = String.format(PLUGIN_PATH_PROPERTY, 0);
        List<String> paths = new ArrayList<>();

        while (environment.containsProperty(key)) {
            String pathProperty = environment.getProperty(key);
            paths.add(pathProperty);

            key = String.format(PLUGIN_PATH_PROPERTY, paths.size());
        }

        // Use default host if required
        if (paths.isEmpty()) {
            paths.add(defaultPluginPath);
        }

        pluginsPath = paths.toArray(new String[] {});

        pluginWorkDir = environment.getProperty(PLUGIN_WORK_DIR_PROPERTY);
    }

    public String[] getPluginsPath() {
        return pluginsPath;
    }

    public void setPluginsPath(String[] pluginsPath) {
        this.pluginsPath = pluginsPath;
    }

    public String getPluginWorkDir() {
        return pluginWorkDir;
    }

    public void setPluginWorkDir(String pluginWorkDir) {
        this.pluginWorkDir = pluginWorkDir;
    }
}
