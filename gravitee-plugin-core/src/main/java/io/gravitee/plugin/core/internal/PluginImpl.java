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
package io.gravitee.plugin.core.internal;

import io.gravitee.plugin.core.api.Plugin;
import io.gravitee.plugin.core.api.PluginManifest;

import java.net.URL;
import java.nio.file.Path;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class PluginImpl implements Plugin {

    private Path path;
    private PluginManifest manifest;
    private URL[] dependencies;

    PluginImpl(PluginManifest manifest) {
        this.manifest = manifest;
    }

    @Override
    public String id() {
        return manifest.id();
    }

    @Override
    public String clazz() {
        return manifest.plugin();
    }

    @Override
    public String type() {
        return manifest.type();
    }

    @Override
    public Path path() {
        return path;
    }

    @Override
    public PluginManifest manifest() {
        return manifest;
    }

    @Override
    public URL[] dependencies() {
        return dependencies;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public void setDependencies(URL[] dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PluginImpl plugin = (PluginImpl) o;

        return manifest.id().equals(plugin.id()) && manifest.type().equals(plugin.type());
    }

    @Override
    public int hashCode() {
        return id().hashCode();
    }
}
