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
import io.gravitee.plugin.core.api.PluginHandler;
import io.gravitee.plugin.core.api.PluginManifest;
import io.gravitee.plugin.core.internal.PluginDependencyImpl;
import io.gravitee.plugin.core.internal.PluginEventListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class PluginEventListenerTest {

    @InjectMocks
    private PluginEventListener eventListener = new PluginEventListener();

    @Mock
    private Plugin plugin, plugin2;

    @Mock
    private PluginManifest pluginManifest;

    @Mock
    private Collection<PluginHandler> pluginHandlers;


    @Test
    public void shouldLoadSinglePlugin() {
        when(plugin.id()).thenReturn("policy");
        when(plugin.type()).thenReturn("policy");

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
        when(plugin.type()).thenReturn("policy");
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
        when(plugin.type()).thenReturn("policy");

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
        when(plugin2.type()).thenReturn("resource");

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
        when(plugin.type()).thenReturn("policy");
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
    public void shouldLoadWithDependencyOrder() {
        eventListener.setFailOnDuplicate(false);

        final Plugin plugin = mock(Plugin.class);
        final PluginManifest pluginManifest = mock(PluginManifest.class);

        when(plugin.id()).thenReturn("policy-1");
        when(plugin.manifest()).thenReturn(pluginManifest);
        when(plugin.type()).thenReturn("policy");
        when(pluginManifest.dependencies()).thenReturn(Arrays.asList(new PluginDependencyImpl("policy", "policy-2")));

        final Plugin plugin2 = mock(Plugin.class);
        final PluginManifest pluginManifest2 = mock(PluginManifest.class);

        when(plugin2.id()).thenReturn("policy-2");
        when(plugin2.manifest()).thenReturn(pluginManifest2);
        when(plugin2.type()).thenReturn("policy");
        when(pluginManifest2.dependencies()).thenReturn(Arrays.asList(new PluginDependencyImpl("policy", "policy-3")));

        final Plugin plugin3 = mock(Plugin.class);
        final PluginManifest pluginManifest3 = mock(PluginManifest.class);

        when(plugin3.id()).thenReturn("policy-3");
        when(plugin3.manifest()).thenReturn(pluginManifest3);
        when(plugin3.type()).thenReturn("policy");
        when(pluginManifest3.dependencies()).thenReturn(Collections.emptyList());


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
                return plugin2;
            }

            @Override
            public PluginEvent type() {
                return PluginEvent.DEPLOYED;
            }
        });


        eventListener.onEvent(new Event<PluginEvent, Plugin>() {
            @Override
            public Plugin content() {
                return plugin3;
            }

            @Override
            public PluginEvent type() {
                return PluginEvent.DEPLOYED;
            }
        });


        final PluginHandler pluginHandler = mock(PluginHandler.class);
        when(pluginHandlers.stream()).thenAnswer(i -> Stream.of(pluginHandler));
        when(pluginHandler.canHandle(any(Plugin.class))).thenReturn(true);

        eventListener.onEvent(new Event<PluginEvent, Plugin>() {
            @Override
            public Plugin content() {
                return null;
            }

            @Override
            public PluginEvent type() {
                return PluginEvent.ENDED;
            }
        });

        ArgumentCaptor<Plugin> pluginCaptor = ArgumentCaptor.forClass(Plugin.class);

        verify(pluginHandler, times(3)).handle(pluginCaptor.capture());

        final Iterator<Plugin> invocations = pluginCaptor.getAllValues().iterator();

        assertEquals("policy-3", invocations.next().id());
        assertEquals("policy-2", invocations.next().id());
        assertEquals("policy-1", invocations.next().id());
    }
}
