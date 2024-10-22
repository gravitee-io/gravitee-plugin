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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

/**
 * @author Yann TAVERNIER (yann.tavernier at graviteesource.com)
 * @author GraviteeSource Team
 */

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AbstractConfigurablePluginManagerTest {

    static final String FAKE_PLUGIN = "fake-plugin";
    AbstractConfigurablePluginManager<FakePlugin> cut;
    static Map<String, String> properties;

    @BeforeEach
    public void setUp() {
        properties = new HashMap<>();
        cut =
            new AbstractConfigurablePluginManager<>() {
                @Override
                public void register(FakePlugin plugin) {
                    super.register(plugin);
                }
            };
    }

    @Test
    void should_get_null_if_plugin_not_deployed() {
        cut.register(new FakePlugin(false));
        assertNull(cut.get(FAKE_PLUGIN));
    }

    @Test
    void should_get_plugin_if_plugin_not_deployed_but_include_not_deployed() {
        cut.register(new FakePlugin(false));
        Assertions.assertNotNull(cut.get(FAKE_PLUGIN, true));
    }

    @Test
    void should_get_schema_file_with_default_property() throws IOException {
        properties.put("schema", "subfolder_A/schema-form-v1.json");

        cut.register(new FakePlugin());
        final String schema = cut.getSchema(FAKE_PLUGIN);
        assertEquals("{\n    \"schema\": \"subfolder_A / BIS configuration\"\n}\n", schema);
    }

    @Test
    void should_get_first_schema_file() throws IOException {
        cut.register(new FakePlugin());
        final String schema = cut.getSchema(FAKE_PLUGIN);
        assertEquals("{\n    \"schema\": \"configuration\"\n}\n", schema);
    }

    @Test
    void should_get_first_schema_file_in_sub_folder1() throws IOException {
        cut.register(new FakePlugin());
        final String schema = cut.getSchema(FAKE_PLUGIN, "subfolder_1");
        assertEquals("{\n    \"schema\": \"subfolder_1\"\n}\n", schema);
    }

    @Test
    void should_get_first_schema_file_in_sub_folder2() throws IOException {
        cut.register(new FakePlugin());
        final String schema = cut.getSchema(FAKE_PLUGIN, "subfolder_1/subfolder_2");
        assertEquals("{\n    \"schema\": \"subfolder_2\"\n}\n", schema);
    }

    @Test
    void should_get_schema_file_with_property_key() throws IOException {
        properties.put("schema.sharedConfiguration", "subfolder_A/schema-form-v1.json");

        cut.register(new FakePlugin());
        final String schema = cut.getSchema(FAKE_PLUGIN, "schema.sharedConfiguration", false, false);
        assertEquals("{\n    \"schema\": \"subfolder_A / BIS configuration\"\n}\n", schema);
    }

    @Test
    void should_get_schema_file_with_property_key_and_fallback() throws IOException {
        properties.put("schema", "subfolder_1/subfolder_2/schema-form.json");

        cut.register(new FakePlugin());
        final String schema = cut.getSchema(FAKE_PLUGIN, "http_message.schema", true, false);
        assertEquals("{\n    \"schema\": \"subfolder_2\"\n}\n", schema);
    }

    @Test
    void should_get_documentation_file_with_default_property() throws IOException {
        properties.put("documentation", "doc_bis.md");

        cut.register(new FakePlugin());
        final String schema = cut.getDocumentation(FAKE_PLUGIN);
        assertEquals("plugin BIS documentation", schema);
    }

    @Test
    void should_get_first_documentation_file() throws IOException {
        cut.register(new FakePlugin());
        final String schema = cut.getDocumentation(FAKE_PLUGIN);
        assertEquals("plugin documentation", schema);
    }

    @Test
    void should_get_documentation_file_with_property_key() throws IOException {
        properties.put("native_kafka.documentation", "doc_bis.md");

        cut.register(new FakePlugin());
        final String schema = cut.getDocumentation(FAKE_PLUGIN, "native_kafka.documentation", true, false);
        assertEquals("plugin BIS documentation", schema);
    }

    @Test
    void should_get_documentation_file_with_property_key_and_fallback() throws IOException {
        cut.register(new FakePlugin());
        final String schema = cut.getDocumentation(FAKE_PLUGIN, "http_message.documentation", true, false);
        assertEquals("plugin documentation", schema);
    }

    @Test
    void should_get_icon_as_base64() throws IOException {
        cut.register(new FakePlugin());
        properties.put("icon", "images/rest-api.png");
        final String icon = cut.getIcon(FAKE_PLUGIN);
        assertTrue(icon.startsWith("data:image/png;base64"));
    }

    @Test
    void should_get_null_if_file_not_found() throws IOException {
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
                    return AbstractConfigurablePluginManagerTest.properties;
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
