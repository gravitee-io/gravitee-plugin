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

package io.gravitee.plugin.cloudserviceprovider.internal;

import io.gravitee.cloudservice.api.plugin.configuration.CloudServiceProviderConfiguration;
import io.gravitee.plugin.cloudserviceprovider.CloudServiceProviderPlugin;
import io.gravitee.plugin.core.api.Plugin;
import io.gravitee.plugin.core.api.PluginManifest;
import java.net.URL;
import java.nio.file.Path;

/**
 * @author Remi Baptiste (remi.baptiste at graviteesource.com)
 * @author GraviteeSource Team
 */
public class DefaultCloudServiceProviderPlugin implements CloudServiceProviderPlugin {

    private final Plugin plugin;
    private final Class<?> cloudServiceProviderClass;
    private Class<? extends CloudServiceProviderConfiguration> cloudServiceProviderConfigurationClass;

    DefaultCloudServiceProviderPlugin(final Plugin plugin, final Class<?> cloudServiceProviderClass) {
        this.plugin = plugin;
        this.cloudServiceProviderClass = cloudServiceProviderClass;
        this.cloudServiceProviderConfigurationClass = null;
    }

    @Override
    public Class<?> cloudServiceProvider() {
        return cloudServiceProviderClass;
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
    public Class<? extends CloudServiceProviderConfiguration> configuration() {
        return cloudServiceProviderConfigurationClass;
    }

    public void setConfiguration(Class<? extends CloudServiceProviderConfiguration> cloudServiceProviderConfigurationClass) {
        this.cloudServiceProviderConfigurationClass = cloudServiceProviderConfigurationClass;
    }
}
