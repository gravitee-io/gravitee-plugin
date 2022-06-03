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
package io.gravitee.plugin.core.api;

import io.gravitee.plugin.core.internal.PluginDependencyImpl;
import io.gravitee.plugin.core.internal.PluginManifestProperties;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Yann TAVERNIER (yann.tavernier at graviteesource.com)
 * @author GraviteeSource Team
 */
public class PluginManifestFactory {

    /**
     * Create a manifest from a properties object.
     *
     * @param properties The properties object to read.
     * @return A plugin manifest.
     */
    public static PluginManifest create(Properties properties) {
        final String id = properties.getProperty(PluginManifestProperties.MANIFEST_ID_PROPERTY);
        final String description = properties.getProperty(PluginManifestProperties.MANIFEST_DESCRIPTION_PROPERTY);
        final String clazz = properties.getProperty(PluginManifestProperties.MANIFEST_CLASS_PROPERTY);
        final String name = properties.getProperty(PluginManifestProperties.MANIFEST_NAME_PROPERTY);
        final String version = properties.getProperty(PluginManifestProperties.MANIFEST_VERSION_PROPERTY);
        final String type = properties.getProperty(PluginManifestProperties.MANIFEST_TYPE_PROPERTY);
        final String category = properties.getProperty(PluginManifestProperties.MANIFEST_CATEGORY_PROPERTY);
        final int priority = Integer.parseInt(properties.getProperty(PluginManifestProperties.MANIFEST_PRIORITY_PROPERTY, "1000"));
        final List<PluginDependency> dependencies = Stream
            .of(properties.getProperty(PluginManifestProperties.MANIFEST_DEPENDENCIES_PROPERTY, "").split(","))
            .filter(s -> !"".equals(s))
            .map(dependencyStr -> {
                final String[] split = dependencyStr.split(":");

                if (split.length == 1) {
                    return new PluginDependencyImpl(split[0], "*");
                } else {
                    return new PluginDependencyImpl(split[0], split[1]);
                }
            })
            .collect(Collectors.toList());

        final Map<String, String> propertiesMap = new HashMap<>();
        properties.forEach((o, o2) -> {
            String key = o.toString();
            if (!PluginManifestProperties.MANIFEST_PROPERTIES.contains(key)) {
                propertiesMap.put(o.toString(), o2.toString());
            }
        });

        return new PluginManifest() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public String description() {
                return description;
            }

            @Override
            public String category() {
                return category;
            }

            @Override
            public String version() {
                return version;
            }

            @Override
            public String plugin() {
                return clazz;
            }

            @Override
            public String type() {
                return type;
            }

            @Override
            public int priority() {
                return priority;
            }

            @Override
            public List<io.gravitee.plugin.core.api.PluginDependency> dependencies() {
                return dependencies;
            }

            @Override
            public Map<String, String> properties() {
                return propertiesMap;
            }
        };
    }
}
