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
package io.gravitee.plugin.fetcher.internal;

import io.gravitee.plugin.fetcher.FetcherPlugin;
import io.gravitee.plugin.fetcher.FetcherPluginManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class FetcherPluginManagerImpl implements FetcherPluginManager {

    private final static String SCHEMAS_DIRECTORY = "schemas";

    private final Map<String, FetcherPlugin> definitions = new HashMap<>();

    @Override
    public void register(FetcherPlugin fetcherPlugin) {
        definitions.put(fetcherPlugin.id(), fetcherPlugin);
    }

    @Override
    public Collection<FetcherPlugin> findAll() {
        return definitions.values();
    }

    @Override
    public FetcherPlugin get(String resource) {
        return definitions.get(resource);
    }

    @Override
    public String getConfiguration(String resource) throws IOException {
        Path resourceWorkspace = get(resource).path();

        File[] schemas = resourceWorkspace.toFile().listFiles(
                pathname -> pathname.isDirectory() && pathname.getName().equals(SCHEMAS_DIRECTORY));

        if (schemas.length == 1) {
            File schemaDir = schemas[0];

            if (schemaDir.listFiles().length > 0) {
                return new String(Files.readAllBytes(schemaDir.listFiles()[0].toPath()));
            }
        }

        return null;
    }
}
