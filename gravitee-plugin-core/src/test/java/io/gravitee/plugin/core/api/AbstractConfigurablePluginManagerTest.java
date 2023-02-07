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
import org.junit.Before;
import org.junit.Test;

/**
 * @author Yann TAVERNIER (yann.tavernier at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AbstractConfigurablePluginManagerTest {

    public static final String FAKE_PLUGIN = "fake-plugin";
    private AbstractConfigurablePluginManager<FakePlugin> cut;

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

    private static class FakePlugin implements ConfigurablePlugin<String> {

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
            return null;
        }

        @Override
        public URL[] dependencies() {
            return new URL[0];
        }
    }
}