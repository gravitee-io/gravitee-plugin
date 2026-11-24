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

import io.gravitee.agentprovider.api.AgentProvider;
import io.gravitee.agentprovider.api.AgentProviderConfiguration;
import io.gravitee.agentprovider.api.AgentProviderFactory;
import io.gravitee.agentprovider.api.model.AgentSnapshot;
import io.gravitee.agentprovider.api.model.DiscoveryPage;
import io.gravitee.agentprovider.api.model.Probe;
import io.gravitee.node.api.cache.CacheManager;
import java.util.List;

/**
 * @author GraviteeSource Team
 */
public class FakeAgentProviderFactory extends AgentProviderFactory<FakeAgentProviderFactory.FakeAgentProvider> {

    public static class FakeConfiguration implements AgentProviderConfiguration {}

    public FakeAgentProviderFactory(CacheManager cacheManager) {
        super(cacheManager);
    }

    @Override
    protected FakeAgentProvider instantiateProvider(String configuration) {
        return new FakeAgentProvider();
    }

    public static class FakeAgentProvider implements AgentProvider, AutoCloseable {

        @Override
        public Probe test() {
            return Probe.ok();
        }

        @Override
        public DiscoveryPage discover(String cursor, int pageSize) {
            return new DiscoveryPage(List.of(), null);
        }

        @Override
        public List<AgentSnapshot> fetch(String... ids) {
            return List.of();
        }

        @Override
        public void close() {}
    }
}
