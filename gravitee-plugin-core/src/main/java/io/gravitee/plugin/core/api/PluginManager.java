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

import java.io.IOException;
import java.util.Collection;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface PluginManager<T extends Plugin> {
    void register(T plugin);

    Collection<T> findAll();

    Collection<T> findAll(boolean includeNotDeployed);

    T get(String pluginId);

    T get(String pluginId, boolean includeNotDeployed);

    String getIcon(String pluginId) throws IOException;

    String getIcon(String pluginId, boolean includeNotDeployed) throws IOException;

    String getDocumentation(String pluginId) throws IOException;

    String getDocumentation(String pluginId, boolean includeNotDeployed) throws IOException;

    String getCategory(String pluginId) throws IOException;

    String getCategory(String pluginId, boolean includeNotDeployed) throws IOException;

    PluginMoreInformation getMoreInformation(String pluginId) throws IOException;

    PluginMoreInformation getMoreInformation(String pluginId, boolean includeNotDeployed) throws IOException;
}
