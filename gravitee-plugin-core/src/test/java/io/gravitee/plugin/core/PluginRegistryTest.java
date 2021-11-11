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

import io.gravitee.common.event.EventManager;
import io.gravitee.plugin.core.api.Plugin;
import io.gravitee.plugin.core.internal.PluginRegistryConfiguration;
import io.gravitee.plugin.core.internal.PluginRegistryImpl;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.*;

/**
 * @author David BRASSELY (brasseld at gmail.com)
 */
public class PluginRegistryTest {

    private static  ExecutorService executor;

    @BeforeClass
    public static void beforeClass() {
        executor = Executors.newFixedThreadPool(2);
    }

    @AfterClass
    public static void afterClass() {
        executor.shutdown();
    }

    @Test(expected = RuntimeException.class)
    public void startWithInvalidWorkspace() throws Exception {
        PluginRegistryImpl pluginRegistry = new PluginRegistryImpl();
        pluginRegistry.start();
    }

    @Test(expected = RuntimeException.class)
    public void startWithInexistantWorkspace() throws Exception {
        PluginRegistryImpl pluginRegistry = new PluginRegistryImpl(
                "/io/gravitee/plugin/invalid/");
        pluginRegistry.start();
    }

    @Test
    public void startWithEmptyWorkspace() throws Exception {
        PluginRegistryImpl pluginRegistry = initPluginRegistry("/io/gravitee/plugin/empty-workspace/");
        pluginRegistry.start();

        Assert.assertTrue(pluginRegistry.plugins().isEmpty());
    }

    @Test
    public void startTwiceWorkspace() throws Exception {
        PluginRegistryImpl pluginRegistry = spy(initPluginRegistry("/io/gravitee/plugin/workspace/"));
        pluginRegistry.start();
        verify(pluginRegistry, atMost(1)).init();

        pluginRegistry.start();
        verify(pluginRegistry, atMost(1)).init();
    }

    @Test
    public void startWithWorkspace_noJar() throws Exception {
        PluginRegistryImpl pluginRegistry = initPluginRegistry("/io/gravitee/plugin/invalid-workspace-nojar/");
        pluginRegistry.start();

        Assert.assertTrue(pluginRegistry.plugins().isEmpty());
    }

    @Test
    public void startWithValidWorkspace_onePolicyDefinition() throws Exception {
        PluginRegistryImpl pluginRegistry = initPluginRegistry("/io/gravitee/plugin/workspace/");
        pluginRegistry.start();

        Assert.assertEquals(1, pluginRegistry.plugins().size());
    }

    @Test
    public void startWithValidWorkspace_checkPluginDescriptor() throws Exception {
        PluginRegistryImpl pluginRegistry = initPluginRegistry("/io/gravitee/plugin/workspace/");
        pluginRegistry.start();

        Assert.assertEquals(1, pluginRegistry.plugins().size());

        Plugin plugin = pluginRegistry.plugins().iterator().next();
        Assert.assertEquals("my-policy", plugin.id());
        Assert.assertEquals("my.project.gravitee.policies.MyPolicy", plugin.clazz());
    }

    @Test
    public void startWithValidWorkspace_withDependencies() throws Exception {
        PluginRegistryImpl pluginRegistry = initPluginRegistry("/io/gravitee/plugin/with-dependencies/");
        pluginRegistry.start();

        Assert.assertEquals(3, pluginRegistry.plugins().size());
    }

    private PluginRegistryImpl initPluginRegistry(String path) throws UnsupportedEncodingException {
        URL dir = PluginRegistryTest.class.getResource(path);
        PluginRegistryImpl pluginRegistry = new PluginRegistryImpl(URLDecoder.decode(dir.getPath(), "UTF-8"));
        pluginRegistry.setEventManager(mock(EventManager.class));
        pluginRegistry.setConfiguration(mock(PluginRegistryConfiguration.class));
        pluginRegistry.setExecutor(executor);
        return pluginRegistry;
    }
}
