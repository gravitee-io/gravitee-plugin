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

import io.gravitee.agentprovider.api.AgentProviderApi;
import io.gravitee.agentprovider.api.AgentProviderConfiguration;
import io.gravitee.agentprovider.api.AgentProviderFactory;
import io.gravitee.agentprovider.api.model.AgentRef;
import io.gravitee.agentprovider.api.model.AgentSnapshot;
import io.gravitee.agentprovider.api.model.Probe;
import java.util.List;

/**
 * @author GraviteeSource Team
 */
public class FakeAgentProviderFactory implements AgentProviderFactory {

    public static class FakeConfiguration implements AgentProviderConfiguration {}

    @Override
    public AgentProviderApi create(String configuration) {
        return new AgentProviderApi() {
            @Override
            public Probe test() {
                return Probe.ok();
            }

            @Override
            public List<AgentRef> discover() {
                return List.of();
            }

            @Override
            public List<AgentSnapshot> fetch(String... ids) {
                return List.of();
            }
        };
    }
}
