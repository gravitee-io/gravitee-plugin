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

import io.gravitee.common.service.Service;
import java.util.Collection;

/**
 * The plugin registry holds a reference on all the plugins that have been loaded.
 * <b>WARN:</b> it just keeps a reference on the plugin that have been detected. It does not mean that the plugin has been properly deployed.
 *
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface PluginRegistry extends Service<PluginRegistry> {
    /**
     * Load all the plugins and fire {@link PluginEvent#BOOT_DEPLOYED} for each plugin that can be handled by a {@link BootPluginHandler}.
     * Once all the plugins have been loaded, a {@link PluginEvent#BOOT_ENDED} is emitted.
     *
     * @throws Exception in case of any error encountered during plugin initialization
     */
    void bootstrap() throws Exception;

    /**
     * Return the list of all the plugins that have been loaded.
     *
     * @return the list of all the plugins that have been loaded.
     */
    Collection<Plugin> plugins();

    /**
     * Return the list of all the plugins of a given <code>type</code> that have been loaded.
     *
     * @return the list of all the plugins that have been loaded for the specified <code>type</code>.
     */
    Collection<Plugin> plugins(String type);

    /**
     * Return the plugin corresponding to the given <code>type</code> and <code>id</code>.
     *
     * @param type the type of the plugin.
     * @param id the id of the plugin.
     *
     * @return the plugin found or <code>null</code> if no corresponding plugin has been found.
     */
    Plugin get(String type, String id);
}
