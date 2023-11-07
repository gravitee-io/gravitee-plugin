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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

import io.gravitee.common.event.EventManager;
import io.gravitee.plugin.core.api.Plugin;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

/**
 * @author David BRASSELY (brasseld at gmail.com)
 */
@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PluginRegistryTest {

    static ExecutorService executor;

    @Mock
    Environment environment;

    @BeforeAll
    static void beforeClass() {
        executor = Executors.newFixedThreadPool(2);
    }

    @AfterAll
    static void afterClass() {
        executor.shutdown();
    }

    @BeforeEach
    void setUp() {
        lenient().when(environment.getProperty(anyString(), any(), any())).thenReturn(true);
    }

    @Test
    void start_with_invalid_workspace() {
        PluginRegistryImpl pluginRegistry = new PluginRegistryImpl(
            mock(PluginRegistryConfiguration.class),
            environment,
            executor,
            mock(EventManager.class)
        );
        assertThatCode(pluginRegistry::start).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void start_with_not_existing_workspace() {
        PluginRegistryImpl pluginRegistry = new PluginRegistryImpl(
            mock(PluginRegistryConfiguration.class),
            environment,
            executor,
            mock(EventManager.class)
        );
        assertThatCode(pluginRegistry::start).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void start_with_empty_workspace() throws Exception {
        PluginRegistryImpl pluginRegistry = initPluginRegistry("/io/gravitee/plugin/empty-workspace/");
        pluginRegistry.start();
        assertThat(pluginRegistry.plugins()).isEmpty();
    }

    @Test
    void start_twice_workspace() throws Exception {
        PluginRegistryImpl pluginRegistry = spy(initPluginRegistry("/io/gravitee/plugin/workspace/"));
        pluginRegistry.start();
        verify(pluginRegistry, atMost(1)).init();

        pluginRegistry.start();
        verify(pluginRegistry, atMost(1)).init();
    }

    @Test
    void start_with_workspace_no_jar() throws Exception {
        PluginRegistryImpl pluginRegistry = initPluginRegistry("/io/gravitee/plugin/invalid-workspace-nojar/");

        pluginRegistry.start();

        assertThat(pluginRegistry.plugins()).isEmpty();
    }

    @Test
    void start_with_valid_workspace_one_policy_definition() throws Exception {
        PluginRegistryImpl pluginRegistry = initPluginRegistry("/io/gravitee/plugin/workspace/");

        pluginRegistry.start();

        assertThat(pluginRegistry.plugins()).hasSize(1);
    }

    @Test
    void start_with_valid_workspace_check_plugin_descriptor() throws Exception {
        PluginRegistryImpl pluginRegistry = initPluginRegistry("/io/gravitee/plugin/workspace/");
        pluginRegistry.start();

        assertThat(pluginRegistry.plugins()).hasSize(1);

        Plugin plugin = pluginRegistry.plugins().iterator().next();
        assertThat(plugin.id()).isEqualTo("my-policy");
        assertThat(plugin.clazz()).isEqualTo("my.project.gravitee.policies.MyPolicy");
    }

    @Test
    void start_with_valid_workspace_with_dependencies() throws Exception {
        PluginRegistryImpl pluginRegistry = initPluginRegistry("/io/gravitee/plugin/with-dependencies/");
        pluginRegistry.start();

        assertThat(pluginRegistry.plugins()).hasSize(4);
    }

    @Test
    void start_with_valid_workspace_with_one_dependency_with_alias_disabled() throws Exception {
        when(environment.containsProperty("policies.my-policy-1.enabled")).thenReturn(true);
        when(environment.getProperty("policies.my-policy-1.enabled", Boolean.class, true)).thenReturn(false);

        PluginRegistryImpl pluginRegistry = initPluginRegistry("/io/gravitee/plugin/with-dependencies/");
        pluginRegistry.start();

        assertThat(pluginRegistry.plugins()).hasSize(3);
        assertThat(pluginRegistry.plugins()).noneMatch(p -> p.id().equals("my-policy-1"));
    }

    @Test
    void start_with_valid_workspace_with_one_dependency_without_alias_disabled() throws Exception {
        when(environment.getProperty("custom.custom-plugin.enabled", Boolean.class, true)).thenReturn(false);

        PluginRegistryImpl pluginRegistry = initPluginRegistry("/io/gravitee/plugin/with-dependencies/");
        pluginRegistry.start();

        assertThat(pluginRegistry.plugins()).hasSize(3);
        assertThat(pluginRegistry.plugins()).noneMatch(p -> p.id().equals("custom-plugin"));
    }

    @Test
    void start_with_valid_workspace_with_duplication() throws Exception {
        String path = "/io/gravitee/plugin/with-duplication/";

        // we are using different version to assert which one is loaded
        updateModifiedTimestampCustomPlugin2();

        PluginRegistryImpl pluginRegistry = initPluginRegistry(path);
        pluginRegistry.start();

        assertThat(pluginRegistry.plugins()).hasSize(1);
        assertThat(pluginRegistry.plugins()).first().extracting(Plugin::id).isEqualTo("custom-plugin");
        assertThat(pluginRegistry.plugins()).first().extracting(p -> p.manifest().version()).isEqualTo("2.0.0-SNAPSHOT");
    }

    @Test
    void start_several_workspaces_with_duplication() throws Exception {
        String path1 = "/io/gravitee/plugin/with-duplication/";
        String path2 = "/io/gravitee/plugin/with-dependencies/";

        // need to the empty constructor thus redo all the config
        PluginRegistryConfiguration configuration = new PluginRegistryConfiguration();
        configuration.setPluginsPath(new String[] { getActualPath(path1), getActualPath(path2) });
        PluginRegistryImpl pluginRegistry = new PluginRegistryImpl(configuration, environment, executor, mock(EventManager.class));

        updateModifiedTimestampCustomPlugin2();
        pluginRegistry.start();

        assertThat(pluginRegistry.plugins()).hasSize(4);
        assertThat(pluginRegistry.plugins())
            .extracting(Plugin::id)
            .containsExactlyInAnyOrder("my-policy-1", "my-policy-2", "my-policy-3", "custom-plugin");
        assertThat(pluginRegistry.plugins())
            .filteredOn(p -> p.id().equals("custom-plugin"))
            .extracting(p -> p.manifest().version())
            .contains("2.0.0-SNAPSHOT");
    }

    @Test
    void should_get_plugin_by_id() throws Exception {
        PluginRegistryImpl pluginRegistry = initPluginRegistry("/io/gravitee/plugin/with-dependencies/");
        pluginRegistry.start();

        final Plugin policy = pluginRegistry.get("policy", "my-policy-3");
        assertThat(policy).isNotNull();
        assertThat(policy.id()).isEqualTo("my-policy-3");
        assertThat(policy.type()).isEqualTo("policy");
    }

    @Test
    void should_return_null_when_getting_unknown_plugin_by_id() throws Exception {
        PluginRegistryImpl pluginRegistry = initPluginRegistry("/io/gravitee/plugin/with-dependencies/");
        pluginRegistry.start();

        final Plugin policy = pluginRegistry.get("policy", "unknown");
        assertThat(policy).isNull();
    }

    @Test
    void should_return_plugins_by_type() throws Exception {
        PluginRegistryImpl pluginRegistry = initPluginRegistry("/io/gravitee/plugin/with-dependencies/");
        pluginRegistry.start();

        final Collection<Plugin> plugins = pluginRegistry.plugins("policy");
        assertThat(plugins).isNotNull();
        assertThat(plugins).hasSize(3);
        assertThat(plugins).allMatch(plugin -> plugin.type().equals("policy"));
    }

    @Test
    void should_have_all_plugin_and_plugins_by_type_equivalent() throws Exception {
        PluginRegistryImpl pluginRegistry = initPluginRegistry("/io/gravitee/plugin/with-dependencies/");
        pluginRegistry.start();

        final Collection<Plugin> allPlugins = pluginRegistry.plugins();
        assertThat(allPlugins).hasSize(4);

        allPlugins.forEach(plugin -> assertThat(allPlugins).containsAll(pluginRegistry.plugins(plugin.type())));
    }

    @Test
    void should_return_empty_when_no_plugins_for_type() throws Exception {
        PluginRegistryImpl pluginRegistry = initPluginRegistry("/io/gravitee/plugin/with-dependencies/");
        pluginRegistry.start();

        final Collection<Plugin> plugins = pluginRegistry.plugins("unknown");
        assertThat(plugins).isNotNull();
        assertThat(plugins).isEmpty();
    }

    private PluginRegistryImpl initPluginRegistry(String path) {
        PluginRegistryImpl pluginRegistry = new PluginRegistryImpl(
            mock(PluginRegistryConfiguration.class),
            environment,
            executor,
            mock(EventManager.class)
        );
        pluginRegistry.setWorkspacesPath(getActualPath(path));
        return pluginRegistry;
    }

    private static String getActualPath(String path) {
        URL dir = PluginRegistryTest.class.getResource(path);
        assertThat(dir).isNotNull();
        return URLDecoder.decode(dir.getPath(), StandardCharsets.UTF_8);
    }

    private static void updateModifiedTimestampCustomPlugin2() throws IOException, InterruptedException {
        String path = "/io/gravitee/plugin/with-duplication/";
        Path pluginFileOlder = Paths.get(getActualPath(path)).resolve("custom-plugin-1.0.0-SNAPSHOT.zip");
        Path pluginFile = Paths.get(getActualPath(path)).resolve("custom-plugin-2.0.0-SNAPSHOT.zip");
        Files.setLastModifiedTime(pluginFile, FileTime.from(Instant.now()));
        assertThat(PluginRegistryImpl.getFileTimestamp(pluginFile)).isGreaterThan(PluginRegistryImpl.getFileTimestamp(pluginFileOlder));
    }
}
