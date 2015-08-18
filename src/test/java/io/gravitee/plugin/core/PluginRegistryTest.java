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
package io.gravitee.plugin.core;

import io.gravitee.plugin.core.api.ClassLoaderFactory;
import io.gravitee.plugin.core.api.Plugin;
import io.gravitee.plugin.core.internal.ClassLoaderFactoryImpl;
import io.gravitee.plugin.core.internal.PluginRegistryImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.URL;

import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.verify;

/**
 * @author David BRASSELY (brasseld at gmail.com)
 */
public class PluginRegistryTest {

    private ClassLoaderFactory classLoaderFactory;

    @Before
    public void setUp() {
        classLoaderFactory = new ClassLoaderFactoryImpl();
    }

    @Test(expected = RuntimeException.class)
    public void initWithInvalidWorkspace() throws Exception {
        PluginRegistryImpl pluginRegistry = new PluginRegistryImpl();
        pluginRegistry.init();
    }

    @Test(expected = RuntimeException.class)
    public void initWithInexistantWorkspace() throws Exception {
        PluginRegistryImpl pluginRegistry = new PluginRegistryImpl(
                "/io/gravitee/plugin/invalid/");
        pluginRegistry.init();
    }

    @Test
    public void initWithEmptyWorkspace() throws Exception {
        URL dir = PluginRegistryTest.class.getResource("/io/gravitee/plugin/empty-workspace/");
        PluginRegistryImpl pluginRegistry = new PluginRegistryImpl(dir.getPath());
        pluginRegistry.init();

        Assert.assertTrue(pluginRegistry.plugins().isEmpty());
    }

    @Test
    public void initTwiceWorkspace() throws Exception {
        URL dir = PluginRegistryTest.class.getResource("/io/gravitee/plugin/workspace/");
        PluginRegistryImpl pluginRegistry = Mockito.spy(new PluginRegistryImpl(dir.getPath()));
        pluginRegistry.setClassLoaderFactory(classLoaderFactory);

        pluginRegistry.init();
        verify(pluginRegistry, atMost(1)).init0();

        pluginRegistry.init();
        verify(pluginRegistry, atMost(1)).init0();
    }

    @Test
    public void initWithWorkspace_noJar() throws Exception {
        URL dir = PluginRegistryTest.class.getResource("/io/gravitee/plugin/invalid-workspace-nojar/");
        PluginRegistryImpl pluginRegistry = new PluginRegistryImpl(dir.getPath());
        pluginRegistry.init();

        Assert.assertTrue(pluginRegistry.plugins().isEmpty());
    }

    @Test
    public void initWithValidWorkspace_onePolicyDefinition() throws Exception {
        URL dir = PluginRegistryTest.class.getResource("/io/gravitee/plugin/workspace/");
        PluginRegistryImpl pluginRegistry = new PluginRegistryImpl(dir.getPath());
        pluginRegistry.setClassLoaderFactory(classLoaderFactory);
        pluginRegistry.init();

        Assert.assertEquals(1, pluginRegistry.plugins().size());
    }

    @Test
    public void initWithValidWorkspace_checkPluginDescriptor() throws Exception {
        URL dir = PluginRegistryTest.class.getResource("/io/gravitee/plugin/workspace/");
        PluginRegistryImpl pluginRegistry = new PluginRegistryImpl(dir.getPath());
        pluginRegistry.setClassLoaderFactory(classLoaderFactory);
        pluginRegistry.init();

        Assert.assertEquals(1, pluginRegistry.plugins().size());

        Plugin plugin = pluginRegistry.plugins().iterator().next();
        Assert.assertEquals("my-policy", plugin.id());
        Assert.assertEquals("my.project.gravitee.policies.MyPolicy", plugin.clazz().getName());
    }
}
