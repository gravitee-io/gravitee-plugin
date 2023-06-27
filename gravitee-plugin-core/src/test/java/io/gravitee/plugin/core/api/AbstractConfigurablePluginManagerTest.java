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

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Yann TAVERNIER (yann.tavernier at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AbstractConfigurablePluginManagerTest {

    public static final String FAKE_PLUGIN = "fake-plugin";
    private AbstractConfigurablePluginManager<FakePlugin> cut;
    private static Map<String, String> properties = new HashMap<>();

    @Before
    public void setUp() {
        cut =
            new AbstractConfigurablePluginManager<FakePlugin>() {
                @Override
                public void register(FakePlugin plugin) {
                    super.register(plugin);
                }
            };
    }

    @Test
    public void shouldGetNullIfPluginNotDeployed() {
        cut.register(new FakePlugin(false));
        assertNull(cut.get(FAKE_PLUGIN));
    }

    @Test
    public void shouldGetPluginIfPluginNotDeployedButIncludeNotDeployed() {
        cut.register(new FakePlugin(false));
        assertNotNull(cut.get(FAKE_PLUGIN, true));
    }

    @Test
    public void shouldGetFirstSchemaFile() throws IOException {
        cut.register(new FakePlugin());
        final String schema = cut.getSchema(FAKE_PLUGIN);
        assertEquals("{\n  \"schema\": \"configuration\"\n}", schema);
    }

    @Test
    public void shouldGetFirstSchemaFileInSubFolder1() throws IOException {
        cut.register(new FakePlugin());
        final String schema = cut.getSchema(FAKE_PLUGIN, "subfolder_1");
        assertEquals("{\n  \"schema\": \"subfolder_1\"\n}", schema);
    }

    @Test
    public void shouldGetFirstSchemaFileInSubFolder2() throws IOException {
        cut.register(new FakePlugin());
        final String schema = cut.getSchema(FAKE_PLUGIN, "subfolder_1/subfolder_2");
        assertEquals("{\n  \"schema\": \"subfolder_2\"\n}", schema);
    }

    @Test
    public void shouldGetFirstDocumentationFile() throws IOException {
        cut.register(new FakePlugin());
        final String schema = cut.getDocumentation(FAKE_PLUGIN);
        assertEquals("plugin documentation", schema);
    }

    @Test
    public void shouldGetIconAsBase64() throws IOException {
        cut.register(new FakePlugin());
        properties.put("icon", "images/rest-api.png");
        final String icon = cut.getIcon(FAKE_PLUGIN);
        assertTrue(icon.startsWith("data:image/png;base64"));
    }

    @Test
    public void shouldGetNullIfFileNotFound() throws IOException {
        cut.register(new FakePlugin());
        properties.put("icon", "images/fake-path.png");
        final String icon = cut.getIcon(FAKE_PLUGIN);
        assertNull(icon);
    }

    private static class FakePlugin implements ConfigurablePlugin<String> {

        boolean deployed;

        public FakePlugin() {
            this.deployed = true;
        }

        public FakePlugin(boolean deployed) {
            this.deployed = deployed;
        }

        @Override
        public Class<String> configuration() {
            return null;
        }

        @Override
        public String id() {
            return FAKE_PLUGIN;
        }

        @Override
        public String clazz() {
            return null;
        }

        @Override
        public String type() {
            return null;
        }

        @Override
        public Path path() {
            try {
                return Paths.get(this.getClass().getClassLoader().getResource("files").toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public PluginManifest manifest() {
            return new PluginManifest() {
                @Override
                public String id() {
                    return FAKE_PLUGIN;
                }

                @Override
                public String name() {
                    return null;
                }

                @Override
                public String description() {
                    return null;
                }

                @Override
                public String category() {
                    return null;
                }

                @Override
                public String version() {
                    return null;
                }

                @Override
                public String plugin() {
                    return null;
                }

                @Override
                public String type() {
                    return null;
                }

                @Override
                public int priority() {
                    return 0;
                }

                @Override
                public String feature() {
                    return null;
                }

                @Override
                public List<PluginDependency> dependencies() {
                    return null;
                }

                @Override
                public Map<String, String> properties() {
                    return properties;
                }
            };
        }

        @Override
        public URL[] dependencies() {
            return new URL[0];
        }

        @Override
        public boolean deployed() {
            return deployed;
        }
    }
}
