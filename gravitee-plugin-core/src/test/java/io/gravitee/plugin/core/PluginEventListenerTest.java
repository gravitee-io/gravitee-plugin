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
package io.gravitee.plugin.core;

import io.gravitee.common.event.Event;
import io.gravitee.plugin.core.api.Plugin;
import io.gravitee.plugin.core.api.PluginEvent;
import io.gravitee.plugin.core.api.PluginManifest;
import io.gravitee.plugin.core.api.PluginType;
import io.gravitee.plugin.core.internal.PluginEventListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class PluginEventListenerTest {

    private PluginEventListener eventListener = new PluginEventListener();

    @Mock
    private Plugin plugin, plugin2;

    @Mock
    private PluginManifest pluginManifest, pluginManifest2;

    @Before
    public void init() {
        initMocks(this);
    }

    @Test
    public void shouldLoadSinglePlugin() {
        when(plugin.id()).thenReturn("policy");
        when(plugin.manifest()).thenReturn(pluginManifest);
        when(plugin.type()).thenReturn(PluginType.POLICY);
        when(pluginManifest.version()).thenReturn("1.0.0-SNAPSHOT");

        eventListener.onEvent(new Event<PluginEvent, Plugin>() {
            @Override
            public Plugin content() {
                return plugin;
            }

            @Override
            public PluginEvent type() {
                return PluginEvent.DEPLOYED;
            }
        });
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFindDuplicatePlugin() {
        eventListener.setFailOnDuplicate(true);

        when(plugin.id()).thenReturn("policy");
        when(plugin.manifest()).thenReturn(pluginManifest);
        when(plugin.type()).thenReturn(PluginType.POLICY);
        when(pluginManifest.version()).thenReturn("1.0.0-SNAPSHOT");

        eventListener.onEvent(new Event<PluginEvent, Plugin>() {
            @Override
            public Plugin content() {
                return plugin;
            }

            @Override
            public PluginEvent type() {
                return PluginEvent.DEPLOYED;
            }
        });

        eventListener.onEvent(new Event<PluginEvent, Plugin>() {
            @Override
            public Plugin content() {
                return plugin;
            }

            @Override
            public PluginEvent type() {
                return PluginEvent.DEPLOYED;
            }
        });
    }

    @Test
    public void shouldNotFindDuplicatePlugin() {
        eventListener.setFailOnDuplicate(true);

        when(plugin.id()).thenReturn("policy");
        when(plugin.manifest()).thenReturn(pluginManifest);
        when(plugin.type()).thenReturn(PluginType.POLICY);
        when(pluginManifest.version()).thenReturn("1.0.0-SNAPSHOT");

        eventListener.onEvent(new Event<PluginEvent, Plugin>() {
            @Override
            public Plugin content() {
                return plugin;
            }

            @Override
            public PluginEvent type() {
                return PluginEvent.DEPLOYED;
            }
        });

        when(plugin2.id()).thenReturn("policy");
        when(plugin2.manifest()).thenReturn(pluginManifest2);
        when(plugin2.type()).thenReturn(PluginType.RESOURCE);
        when(pluginManifest2.version()).thenReturn("1.0.0-SNAPSHOT");

        eventListener.onEvent(new Event<PluginEvent, Plugin>() {
            @Override
            public Plugin content() {
                return plugin2;
            }

            @Override
            public PluginEvent type() {
                return PluginEvent.DEPLOYED;
            }
        });
    }

    @Test
    public void shouldLoadDuplicatePlugin() {
        eventListener.setFailOnDuplicate(false);

        when(plugin.id()).thenReturn("policy");
        when(plugin.manifest()).thenReturn(pluginManifest);
        when(plugin.type()).thenReturn(PluginType.POLICY);
        when(pluginManifest.version()).thenReturn("1.0.0-SNAPSHOT");

        eventListener.onEvent(new Event<PluginEvent, Plugin>() {
            @Override
            public Plugin content() {
                return plugin;
            }

            @Override
            public PluginEvent type() {
                return PluginEvent.DEPLOYED;
            }
        });

        eventListener.onEvent(new Event<PluginEvent, Plugin>() {
            @Override
            public Plugin content() {
                return plugin;
            }

            @Override
            public PluginEvent type() {
                return PluginEvent.DEPLOYED;
            }
        });
    }
}
