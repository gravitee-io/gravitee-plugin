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

import java.util.Objects;

public class PluginMoreInformation {

    private String description;
    private String documentationUrl;
    private String schemaImg;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDocumentationUrl() {
        return documentationUrl;
    }

    public void setDocumentationUrl(String documentationUrl) {
        this.documentationUrl = documentationUrl;
    }

    public String getSchemaImg() {
        return schemaImg;
    }

    public void setSchemaImg(String schemaImg) {
        this.schemaImg = schemaImg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PluginMoreInformation that = (PluginMoreInformation) o;

        if (!Objects.equals(description, that.description)) return false;
        if (!Objects.equals(documentationUrl, that.documentationUrl)) return false;
        return Objects.equals(schemaImg, that.schemaImg);
    }

    @Override
    public int hashCode() {
        int result = description != null ? description.hashCode() : 0;
        result = 31 * result + (documentationUrl != null ? documentationUrl.hashCode() : 0);
        result = 31 * result + (schemaImg != null ? schemaImg.hashCode() : 0);
        return result;
    }
}
