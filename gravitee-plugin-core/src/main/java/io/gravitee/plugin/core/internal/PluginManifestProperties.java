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
package io.gravitee.plugin.core.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface PluginManifestProperties {
    String MANIFEST_ID_PROPERTY = "id";
    String MANIFEST_NAME_PROPERTY = "name";
    String MANIFEST_VERSION_PROPERTY = "version";
    String MANIFEST_DESCRIPTION_PROPERTY = "description";
    String MANIFEST_CLASS_PROPERTY = "class";
    String MANIFEST_TYPE_PROPERTY = "type";
    String MANIFEST_ICON_PROPERTY = "icon";
    String MANIFEST_CATEGORY_PROPERTY = "category";
    String MANIFEST_PRIORITY_PROPERTY = "priority";
    String MANIFEST_DEPENDENCIES_PROPERTY = "dependencies";
    String MANIFEST_FEATURE_PROPERTY = "feature";
    String SCHEMA_PROPERTY = "schema";
    String DOCUMENTATION_PROPERTY = "documentation";
    String MORE_INFO_DESCRIPTION_PROPERTY = "moreInfo.description";
    String MORE_INFO_DOCUMENTATION_URL_PROPERTY = "moreInfo.documentationUrl";
    String MORE_INFO_SCHEMA_IMG_PROPERTY = "moreInfo.schemaImg";

    Set<String> MANIFEST_PROPERTIES = new HashSet<>(
        Arrays.asList(
            MANIFEST_ID_PROPERTY,
            MANIFEST_NAME_PROPERTY,
            MANIFEST_VERSION_PROPERTY,
            MANIFEST_DESCRIPTION_PROPERTY,
            MANIFEST_CLASS_PROPERTY,
            MANIFEST_TYPE_PROPERTY
        )
    );
}
