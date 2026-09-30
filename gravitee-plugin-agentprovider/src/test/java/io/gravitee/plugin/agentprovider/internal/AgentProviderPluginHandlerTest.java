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
package io.gravitee.plugin.agentprovider.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.gravitee.plugin.core.api.Plugin;
import io.gravitee.plugin.core.api.PluginContextConfigurer;
import io.gravitee.plugin.core.api.PluginContextFactory;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

/**
 * @author GraviteeSource Team
 */
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AgentProviderPluginHandlerTest {

    @Nested
    class CanHandle {

        private final AgentProviderPluginHandler cut = new AgentProviderPluginHandler();

        @ParameterizedTest
        @ValueSource(strings = { "agent-provider", "AGENT-PROVIDER", "Agent-Provider" })
        void should_handle_agent_provider_plugins(String type) {
            assertThat(cut.canHandle(pluginOfType(type))).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = { "integration-provider", "policy", "agent_provider", "" })
        void should_not_handle_other_plugins(String type) {
            assertThat(cut.canHandle(pluginOfType(type))).isFalse();
        }

        private static Plugin pluginOfType(String type) {
            Plugin plugin = mock(Plugin.class);
            when(plugin.type()).thenReturn(type);
            return plugin;
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class Handle {

        @Mock
        private PluginContextFactory pluginContextFactory;

        @Mock
        private ApplicationContext pluginContext;

        @Spy
        private DefaultAgentProviderPluginManager agentProviderPluginManager = new DefaultAgentProviderPluginManager();

        @InjectMocks
        private AgentProviderPluginHandler cut;

        @Test
        void should_register_the_factory_bean_of_a_deployed_plugin() {
            FakeAgentProviderFactory factory = new FakeAgentProviderFactory();
            when(pluginContextFactory.create(any(PluginContextConfigurer.class))).thenReturn(pluginContext);
            when(pluginContext.getBean(FakeAgentProviderFactory.class)).thenReturn(factory);

            cut.handle(new FakeAgentProviderPlugin("fake", FakeAgentProviderFactory.class, true), FakeAgentProviderFactory.class);

            assertThat(agentProviderPluginManager.get("fake")).isNotNull();
            assertThat(agentProviderPluginManager.getAgentProviderFactory("fake")).isSameAs(factory);
        }

        @Test
        void should_register_an_undeployed_plugin_without_creating_its_context() {
            cut.handle(new FakeAgentProviderPlugin("fake", FakeAgentProviderFactory.class, false), FakeAgentProviderFactory.class);

            verifyNoInteractions(pluginContextFactory);
            assertThat(agentProviderPluginManager.get("fake", true)).isNotNull();
            assertThat(agentProviderPluginManager.getAgentProviderFactory("fake")).isNull();
        }

        @Test
        void should_discard_the_plugin_context_when_the_factory_bean_cannot_be_created() {
            ArgumentCaptor<PluginContextConfigurer> configurer = ArgumentCaptor.forClass(PluginContextConfigurer.class);
            when(pluginContextFactory.create(configurer.capture())).thenReturn(pluginContext);
            when(pluginContext.getBean(FakeAgentProviderFactory.class)).thenThrow(
                new NoSuchBeanDefinitionException(FakeAgentProviderFactory.class)
            );

            cut.handle(new FakeAgentProviderPlugin("fake", FakeAgentProviderFactory.class, true), FakeAgentProviderFactory.class);

            verify(pluginContextFactory).remove(configurer.getValue().plugin());
            assertThat(agentProviderPluginManager.get("fake")).isNotNull();
            assertThat(agentProviderPluginManager.getAgentProviderFactory("fake")).isNull();
        }
    }
}
