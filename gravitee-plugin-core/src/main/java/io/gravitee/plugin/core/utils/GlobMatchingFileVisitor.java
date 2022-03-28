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
package io.gravitee.plugin.core.utils;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * @author David BRASSELY (brasseld at gmail.com)
 */
public class GlobMatchingFileVisitor extends SimpleFileVisitor<Path> {

    private final PathMatcher matcher;

    /**
     * Path list result
     */
    private List<Path> matchedPaths = new ArrayList<>();

    public GlobMatchingFileVisitor(String pattern) {
        matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attribs) {
        Path name = file.getFileName();
        if (matcher.matches(name)) {
            matchedPaths.add(file);
        }
        return FileVisitResult.CONTINUE;
    }

    public List<Path> getMatchedPaths() {
        return matchedPaths;
    }
}
