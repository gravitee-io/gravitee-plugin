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
package io.gravitee.plugin.policy.internal;

import io.gravitee.plugin.core.api.Plugin;
import io.gravitee.plugin.core.api.PluginManifest;
import io.gravitee.plugin.policy.PolicyPlugin;
import io.gravitee.policy.api.PolicyConfiguration;
import io.gravitee.policy.api.PolicyContext;
import java.net.URL;
import java.nio.file.Path;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
class PolicyPluginImpl implements PolicyPlugin {

    private final Plugin plugin;
    private final Class<?> policyClass;
    private Class<? extends PolicyConfiguration> policyConfigurationClass;
    private Class<? extends PolicyContext> policyContextClass;

    PolicyPluginImpl(final Plugin plugin, final Class<?> policyClass) {
        this.plugin = plugin;
        this.policyClass = policyClass;
        this.policyConfigurationClass = null;
    }

    @Override
    public Class<? extends PolicyContext> context() {
        return policyContextClass;
    }

    @Override
    public Class<?> policy() {
        return policyClass;
    }

    @Override
    public String clazz() {
        return plugin.clazz();
    }

    @Override
    public URL[] dependencies() {
        return plugin.dependencies();
    }

    @Override
    public String id() {
        return plugin.id();
    }

    @Override
    public PluginManifest manifest() {
        return plugin.manifest();
    }

    @Override
    public Path path() {
        return plugin.path();
    }

    @Override
    public boolean deployed() {
        return plugin.deployed();
    }

    @Override
    public Class<? extends PolicyConfiguration> configuration() {
        return policyConfigurationClass;
    }

    public void setConfiguration(Class<? extends PolicyConfiguration> policyConfigurationClass) {
        this.policyConfigurationClass = policyConfigurationClass;
    }

    public void setContext(Class<? extends PolicyContext> policyContextClass) {
        this.policyContextClass = policyContextClass;
    }
}
