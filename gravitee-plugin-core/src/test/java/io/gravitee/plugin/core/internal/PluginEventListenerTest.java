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
package io.gravitee.plugin.core.internal;

import static io.gravitee.plugin.core.api.PluginEvent.DEPLOYED;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.gravitee.common.event.Event;
import io.gravitee.common.event.EventManager;
import io.gravitee.common.event.impl.EventManagerImpl;
import io.gravitee.plugin.core.api.*;
import java.util.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PluginEventListenerTest {

    PluginEventListener eventListener;

    EventManager eventManager;

    private List<Plugin> handledPlugins;

    @BeforeEach
    void setup() throws Exception {
        Collection<PluginHandler> pluginHandlers = new ArrayList<>();
        pluginHandlers.add(
            new PluginHandler() {
                @Override
                public boolean canHandle(Plugin plugin) {
                    return true;
                }

                @Override
                public void handle(Plugin plugin) {
                    handledPlugins.add(plugin);
                }
            }
        );
        handledPlugins = new ArrayList<>();
        eventManager = new EventManagerImpl();
        this.eventListener = new PluginEventListener(pluginHandlers, eventManager);
        this.eventListener.doStart();
    }

    @Test
    void should_load_single_plugin() {
        final Plugin plugin = createPlugin("custom-1", "custom", null);

        eventManager.publishEvent(DEPLOYED, plugin);

        assertThat(eventListener.getPlugins()).hasSize(1);

        eventManager.publishEvent(PluginEvent.ENDED, null);

        assertThat(handledPlugins).containsExactly(plugin);
    }

    @Test
    void should_load_with_dependency_order() {
        final Plugin plugin1 = createPlugin("policy-1", "policy", "policy:policy-2");
        final Plugin plugin2 = createPlugin("policy-2", "policy", "policy:policy-3");
        final Plugin plugin3 = createPlugin("policy-3", "policy", null);

        eventManager.publishEvent(DEPLOYED, plugin1);
        eventManager.publishEvent(DEPLOYED, plugin2);
        eventManager.publishEvent(DEPLOYED, plugin3);

        assertThat(eventListener.getPlugins()).containsValues(plugin1, plugin2, plugin3);

        eventManager.publishEvent(PluginEvent.ENDED, null);

        // handled from leaf to trunk
        assertThat(handledPlugins).containsExactly(plugin3, plugin2, plugin1);
    }

    private static Plugin createPlugin(String id, String type, String dependency) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(type);

        Properties manifestProperties = new Properties();
        manifestProperties.put(PluginManifestProperties.MANIFEST_ID_PROPERTY, id);
        manifestProperties.put(PluginManifestProperties.MANIFEST_TYPE_PROPERTY, type);
        if (dependency != null) {
            manifestProperties.put(PluginManifestProperties.MANIFEST_DEPENDENCIES_PROPERTY, dependency);
        }
        return new PluginImpl(PluginManifestFactory.create(manifestProperties));
    }
}
