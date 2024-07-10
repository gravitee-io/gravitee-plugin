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

import io.gravitee.plugin.cloudserviceprovider.CloudServiceProviderClassLoaderFactory;
import io.gravitee.plugin.cloudserviceprovider.CloudServiceProviderPlugin;
import io.gravitee.plugin.core.internal.PluginClassLoaderFactoryImpl;

/**
 * @author Remi Baptiste (remi.baptiste at graviteesource.com)
 * @author GraviteeSource Team
 */
public class DefaultCloudServiceProviderClassLoaderFactory
    extends PluginClassLoaderFactoryImpl<CloudServiceProviderPlugin>
    implements CloudServiceProviderClassLoaderFactory {}
