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
package io.gravitee.plugin;

import io.gravitee.plugin.impl.ClassLoaderFactoryImpl;
import io.gravitee.plugin.impl.PluginManagerImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.verify;

/**
 * @author David BRASSELY (brasseld at gmail.com)
 */
public class PluginManagerTest {

    private ClassLoaderFactory classLoaderFactory;
    private Collection<PluginHandler> pluginHandlers;

    @Before
    public void setUp() {
        classLoaderFactory = new ClassLoaderFactoryImpl();

        /*
        PolicyManagerImpl policyManager = new PolicyManagerImpl();
        policyManager.setPolicyMethodResolver(new PolicyMethodResolverImpl());
        policyManager.setPluginContextFactory(new PluginContextFactoryImpl());
        */

        pluginHandlers = new ArrayList<>();

        /*
        pluginHandlers.add(policyManager);
        pluginHandlers.add(new ReporterManagerImpl());
        */
    }

    @Test(expected = RuntimeException.class)
    public void initWithInvalidWorkspace() throws Exception {
        PluginManagerImpl pluginManager = new PluginManagerImpl();
        pluginManager.initialize();
    }

    @Test(expected = RuntimeException.class)
    public void initWithInexistantWorkspace() throws Exception {
        PluginManagerImpl pluginManager = new PluginManagerImpl(
                "/io/gravitee/plugin/invalid/");
        pluginManager.initialize();
    }

    @Test
    public void initWithEmptyWorkspace() throws Exception {
        URL dir = PluginManagerTest.class.getResource("/io/gravitee/plugin/empty-workspace/");
        PluginManagerImpl pluginManager = new PluginManagerImpl(dir.getPath());
        pluginManager.initialize();

        Assert.assertTrue(pluginManager.getPlugins().isEmpty());
    }

    @Test
    public void initTwiceWorkspace() throws Exception {
        URL dir = PluginManagerTest.class.getResource("/io/gravitee/plugin/workspace/");
        PluginManagerImpl pluginManager = Mockito.spy(new PluginManagerImpl(dir.getPath()));
        pluginManager.setClassLoaderFactory(classLoaderFactory);
        pluginManager.setPluginHandlers(pluginHandlers);

        pluginManager.initialize();
        verify(pluginManager, atMost(1)).initializeFromWorkspace();

        pluginManager.initialize();
        verify(pluginManager, atMost(1)).initializeFromWorkspace();
    }

    @Test
    public void initWithWorkspace_noJar() throws Exception {
        URL dir = PluginManagerTest.class.getResource("/io/gravitee/plugin/invalid-workspace-nojar/");
        PluginManagerImpl pluginManager = new PluginManagerImpl(dir.getPath());
        pluginManager.initialize();

        Assert.assertTrue(pluginManager.getPlugins().isEmpty());
    }

    @Test
    public void initWithValidWorkspace_onePolicyDefinition() throws Exception {
        URL dir = PluginManagerTest.class.getResource("/io/gravitee/plugin/workspace/");
        PluginManagerImpl pluginManager = new PluginManagerImpl(dir.getPath());
        pluginManager.setClassLoaderFactory(classLoaderFactory);
        pluginManager.setPluginHandlers(pluginHandlers);
        pluginManager.initialize();

        Assert.assertEquals(1, pluginManager.getPlugins().size());
    }

    @Test
    public void initWithValidWorkspace_checkPluginDescriptor() throws Exception {
        URL dir = PluginManagerTest.class.getResource("/io/gravitee/plugin/workspace/");
        PluginManagerImpl pluginManager = new PluginManagerImpl(dir.getPath());
        pluginManager.setClassLoaderFactory(classLoaderFactory);
        pluginManager.setPluginHandlers(pluginHandlers);
        pluginManager.initialize();

        Assert.assertEquals(1, pluginManager.getPlugins().size());

        Plugin plugin = pluginManager.getPlugins().iterator().next();
        Assert.assertEquals("my-policy", plugin.id());
        Assert.assertEquals("my.project.gravitee.policies.MyPolicy", plugin.clazz().getName());
    }
}
