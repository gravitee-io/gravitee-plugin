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
import static org.mockito.Mockito.mock;

import io.gravitee.agentprovider.api.AgentProviderApi;
import io.gravitee.agentprovider.api.AgentProviderFactory;
import io.gravitee.plugin.agentprovider.spring.AgentProviderPluginConfiguration;
import io.gravitee.plugin.api.PluginDeploymentContextFactory;
import io.gravitee.plugin.core.api.PluginClassLoaderFactory;
import io.gravitee.plugin.core.api.PluginConfigurationResolver;
import io.gravitee.plugin.core.api.PluginContextFactory;
import io.gravitee.plugin.core.internal.CachedPluginClassLoaderFactory;
import io.gravitee.plugin.core.internal.PluginContextFactoryImpl;
import io.gravitee.plugin.core.internal.ReflectionBasedPluginConfigurationResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Wires the handler against the real plugin context machinery of the core, as a host node does.
 *
 * @author GraviteeSource Team
 */
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AgentProviderPluginContextTest {

    private AnnotationConfigApplicationContext host;

    @BeforeEach
    void setUp() {
        host = new AnnotationConfigApplicationContext();
        host.registerBean(HostService.class);
        host.registerBean("pluginClassLoaderFactory", PluginClassLoaderFactory.class, CachedPluginClassLoaderFactory::new);
        host.registerBean(PluginConfigurationResolver.class, ReflectionBasedPluginConfigurationResolver::new);
        host.registerBean(PluginContextFactory.class, PluginContextFactoryImpl::new);
        host.registerBean(PluginDeploymentContextFactory.class, () -> mock(PluginDeploymentContextFactory.class));
        host.register(AgentProviderPluginConfiguration.class);
        host.refresh();
    }

    @AfterEach
    void tearDown() {
        host.close();
    }

    @Test
    void should_inject_host_beans_into_the_factory_of_a_plugin() {
        host
            .getBean(AgentProviderPluginHandler.class)
            .handle(
                new FakeAgentProviderPlugin("host-aware", HostAwareAgentProviderFactory.class, true),
                HostAwareAgentProviderFactory.class
            );

        AgentProviderFactory factory = host.getBean(DefaultAgentProviderPluginManager.class).getAgentProviderFactory("host-aware");
        assertThat(factory).isInstanceOf(HostAwareAgentProviderFactory.class);
        assertThat(((HostAwareAgentProviderFactory) factory).hostService).isSameAs(host.getBean(HostService.class));
    }

    public static class HostService {}

    public static class HostAwareAgentProviderFactory implements AgentProviderFactory {

        @Autowired
        HostService hostService;

        @Override
        public AgentProviderApi create(String configuration) {
            return null;
        }
    }
}
