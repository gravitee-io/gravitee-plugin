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
package io.gravitee.plugin.core.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

/**
 * @author David BRASSELY (david at gravitee.io)
 * @author GraviteeSource Team
 */
public final class PluginClassLoader extends ClassLoader {

    private URLClassLoader classLoader;

    public PluginClassLoader(URLClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void close() throws IOException {
        this.classLoader.close();
    }

    @Override
    public URL findResource(String name) {
        return classLoader.findResource(name);
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        return classLoader.findResources(name);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return classLoader.getResourceAsStream(name);
    }

    @Override
    public void clearAssertionStatus() {
        classLoader.clearAssertionStatus();
    }

    @Override
    public URL getResource(String name) {
        return classLoader.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return classLoader.getResources(name);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return classLoader.loadClass(name);
    }

    @Override
    public void setClassAssertionStatus(String className, boolean enabled) {
        classLoader.setClassAssertionStatus(className, enabled);
    }

    @Override
    public void setDefaultAssertionStatus(boolean enabled) {
        classLoader.setDefaultAssertionStatus(enabled);
    }

    @Override
    public void setPackageAssertionStatus(String packageName, boolean enabled) {
        classLoader.setPackageAssertionStatus(packageName, enabled);
    }

    /**
     * This is needed by ClassGraph
     * @return
     */
    public URL[] getURLs() {
        return classLoader.getURLs();
    }
}
