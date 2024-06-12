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
package io.gravitee.plugin.core.utils;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import org.junit.Test;

/**
 * @author Brian Krug
 */
public class FileUtilsTest {

    @Test
    public void createZipFileSystem() throws IOException {
        checkZipFileSystem(
            "/io/gravitee/plugin/with-dependencies/my-policy-1-1.0.0-SNAPSHOT.zip",
            "my-policy-1.0.0-SNAPSHOT.jar",
            "schemas/urn:jsonschema:my:project:gravitee:policies:MyPolicyConfiguration.json"
        );
        checkZipFileSystem(
            "/io/gravitee/plugin/workspace with a space/my policy with spaces-1.0.0-SNAPSHOT.zip",
            "my-policy-1.0.0-SNAPSHOT.jar",
            "schemas/urn:jsonschema:my:project:gravitee:policies:MyPolicyConfiguration.json"
        );
    }

    private static void checkZipFileSystem(String zipFile, String... expectedFilePaths) throws IOException {
        try (FileSystem zfs = FileUtils.createZipFileSystem(getActualPath(zipFile), false)) {
            Iterator<Path> roots = zfs.getRootDirectories().iterator();
            assertTrue("Zip FileSystem has no roots", roots.hasNext());
            Path root = roots.next();
            for (String expectedFilePath : expectedFilePaths) {
                assertTrue("Path '" + expectedFilePath + "' not found in the Zip FileSystem", Files.exists(root.resolve(expectedFilePath)));
            }
        }
    }

    private static String getActualPath(String path) throws UnsupportedEncodingException {
        URL dir = FileUtilsTest.class.getResource(path);
        assertNotNull(dir);
        return URLDecoder.decode(dir.getPath(), StandardCharsets.UTF_8.name());
    }
}
