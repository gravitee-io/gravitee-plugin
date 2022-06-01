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

import io.gravitee.plugin.core.api.PluginManifestValidator;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author David BRASSELY (brasseld at gmail.com)
 */
public class PropertiesBasedPluginManifestValidator implements PluginManifestValidator {

    protected final Logger LOGGER = LoggerFactory.getLogger(PropertiesBasedPluginManifestValidator.class);

    private final Properties properties;

    private static final String[] DESCRIPTOR_PROPERTIES = new String[] {
        PluginManifestProperties.MANIFEST_ID_PROPERTY,
        PluginManifestProperties.MANIFEST_DESCRIPTION_PROPERTY,
        PluginManifestProperties.MANIFEST_CLASS_PROPERTY,
        PluginManifestProperties.MANIFEST_NAME_PROPERTY,
        PluginManifestProperties.MANIFEST_VERSION_PROPERTY,
        PluginManifestProperties.MANIFEST_TYPE_PROPERTY,
    };

    public PropertiesBasedPluginManifestValidator(Properties properties) {
        this.properties = properties;
    }

    @Override
    public boolean validate() {
        for (String key : DESCRIPTOR_PROPERTIES) {
            if (!validate(key)) {
                LOGGER.error("The property {} is not valid", key);
                return false;
            }
        }

        return true;
    }

    private boolean validate(String key) {
        final String value = properties.getProperty(key);
        return (value != null && !value.isEmpty());
    }
}
