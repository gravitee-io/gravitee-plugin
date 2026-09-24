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

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

/**
 * @author GraviteeSource Team
 */
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class DefaultAgentProviderPluginManagerTest {

    private final DefaultAgentProviderPluginManager cut = new DefaultAgentProviderPluginManager();

    @Test
    void should_expose_the_factory_registered_with_a_plugin() {
        FakeAgentProviderFactory factory = new FakeAgentProviderFactory();

        cut.register(new FakeAgentProviderPlugin("fake", FakeAgentProviderFactory.class, true), factory);

        assertThat(cut.getAgentProviderFactory("fake")).isSameAs(factory);
        assertThat(cut.get("fake")).isNotNull();
    }

    @Test
    void should_not_expose_a_factory_for_a_plugin_registered_without_one() {
        cut.register(new FakeAgentProviderPlugin("fake", FakeAgentProviderFactory.class, true));

        assertThat(cut.get("fake")).isNotNull();
        assertThat(cut.getAgentProviderFactory("fake")).isNull();
    }

    @Test
    void should_return_null_for_unknown_plugin() {
        assertThat(cut.getAgentProviderFactory("unknown")).isNull();
    }
}
