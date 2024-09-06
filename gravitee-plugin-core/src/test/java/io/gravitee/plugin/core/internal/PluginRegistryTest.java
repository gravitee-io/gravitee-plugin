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
import io.gravitee.plugin.core.api.BootPluginHandler;
import io.gravitee.plugin.core.api.Plugin;
import io.gravitee.plugin.core.api.PluginEvent;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.AllArgsConstructor;
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

    @Mock
    EventManager eventManager;

    private List<BootPluginHandler> bootPluginHandlers;

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
        bootPluginHandlers = new ArrayList<>();
        lenient().when(environment.getProperty(anyString(), any(), any())).thenReturn(true);
    }

    @Test
    void start_with_invalid_workspace() {
        PluginRegistryImpl pluginRegistry = new PluginRegistryImpl(
            mock(PluginRegistryConfiguration.class),
            environment,
            executor,
            eventManager,
            bootPluginHandlers
        );
        assertThatCode(pluginRegistry::start).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void start_with_not_existing_workspace() {
        PluginRegistryImpl pluginRegistry = new PluginRegistryImpl(
            mock(PluginRegistryConfiguration.class),
            environment,
            executor,
            eventManager,
            bootPluginHandlers
        );
        assertThatCode(pluginRegistry::start).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void start_with_empty_workspace() throws Exception {
        PluginRegistryImpl pluginRegistry = initPluginRegistry("/io/gravitee/plugin/empty-workspace/");
        pluginRegistry.start();

        assertThat(pluginRegistry.plugins()).isEmpty();
        verify(eventManager).publishEvent(eq(PluginEvent.ENDED), any());
    }

    @Test
    void start_twice_workspace() throws Exception {
        PluginRegistryImpl pluginRegistry = spy(initPluginRegistry("/io/gravitee/plugin/workspace/"));
        pluginRegistry.start();
        assertThat(pluginRegistry.plugins()).hasSize(1);

        verify(eventManager, times(pluginRegistry.plugins().size())).publishEvent(eq(PluginEvent.DEPLOYED), any());
        verify(eventManager).publishEvent(eq(PluginEvent.ENDED), any());

        pluginRegistry.start();
        assertThat(pluginRegistry.plugins()).hasSize(1);
        verifyNoMoreInteractions(eventManager);
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
        when(environment.containsProperty("policies.my-policy-2.enabled")).thenReturn(true);
        when(environment.containsProperty("policies.my-policy-3.enabled")).thenReturn(true);
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
        PluginRegistryImpl pluginRegistry = new PluginRegistryImpl(
            configuration,
            environment,
            executor,
            mock(EventManager.class),
            bootPluginHandlers
        );

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

    @Test
    void should_do_nothing_when_bootstrap_with_no_boot_plugin_handler() throws Exception {
        PluginRegistryImpl pluginRegistry = initPluginRegistry("/io/gravitee/plugin/with-dependencies/");
        pluginRegistry.bootstrap();

        verify(eventManager, never()).publishEvent(eq(PluginEvent.BOOT_DEPLOYED), any());
        verify(eventManager).publishEvent(eq(PluginEvent.BOOT_ENDED), any());
    }

    @Test
    void should_emit_no_boot_deployed_event_when_bootstrap_with_plugins_not_handled() throws Exception {
        bootPluginHandlers.add(new FakeBootPluginHandler(false));
        PluginRegistryImpl pluginRegistry = initPluginRegistry("/io/gravitee/plugin/with-dependencies/");

        pluginRegistry.bootstrap();

        verify(eventManager, never()).publishEvent(eq(PluginEvent.BOOT_DEPLOYED), any());
        verify(eventManager).publishEvent(eq(PluginEvent.BOOT_ENDED), any());
    }

    @Test
    void should_emit_boot_deployed_event_when_bootstrap_with_plugins() throws Exception {
        bootPluginHandlers.add(new FakeBootPluginHandler(true));
        PluginRegistryImpl pluginRegistry = initPluginRegistry("/io/gravitee/plugin/with-dependencies/");

        pluginRegistry.bootstrap();

        verify(eventManager, times(pluginRegistry.plugins().size())).publishEvent(eq(PluginEvent.BOOT_DEPLOYED), any());
        verify(eventManager).publishEvent(eq(PluginEvent.BOOT_ENDED), any());
    }

    @Test
    void should_emit_boot_ended_event_only_once_when_bootstrap_and_then_start() throws Exception {
        bootPluginHandlers.add(new FakeBootPluginHandler(false));
        PluginRegistryImpl pluginRegistry = initPluginRegistry("/io/gravitee/plugin/with-dependencies/");

        pluginRegistry.bootstrap();
        pluginRegistry.start();

        verify(eventManager, never()).publishEvent(eq(PluginEvent.BOOT_DEPLOYED), any());
        verify(eventManager).publishEvent(eq(PluginEvent.BOOT_ENDED), any());

        verify(eventManager, times(pluginRegistry.plugins().size())).publishEvent(eq(PluginEvent.DEPLOYED), any());
        verify(eventManager).publishEvent(eq(PluginEvent.ENDED), any());
    }

    private PluginRegistryImpl initPluginRegistry(String path) {
        PluginRegistryImpl pluginRegistry = new PluginRegistryImpl(
            mock(PluginRegistryConfiguration.class),
            environment,
            executor,
            eventManager,
            bootPluginHandlers
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

    @AllArgsConstructor
    private static class FakeBootPluginHandler implements BootPluginHandler {

        private final boolean canHandle;

        @Override
        public boolean canHandle(Plugin plugin) {
            return canHandle;
        }

        @Override
        public void handle(Plugin plugin) {
            // Ignore.
        }
    }
}
