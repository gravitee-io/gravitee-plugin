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
package io.gravitee.plugin.annotation.processor;

import static com.google.testing.compile.Compiler.javac;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.JavaFileObjectSubject;
import com.google.testing.compile.JavaFileObjects;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.tools.JavaFileObject;
import org.junit.Test;

/**
 * @author Remi Baptiste (remi.baptiste at graviteesource.com)
 * @author GraviteeSource Team
 */
public class ConfigurationEvaluatorProcessorTest {

    private static final JavaFileObject RESULT = JavaFileObjects.forResource("test/TestConfigurationEvaluator.java");

    @Test
    public void shouldGenerateTheExpectedResultFile() throws IOException {
        ConfigurationEvaluatorProcessor processor = new ConfigurationEvaluatorProcessor();

        // Run
        Compilation compilation = javac()
            .withProcessors(processor)
            .compile(readJavaFilesFromFolder("src/test/java/io/gravitee/plugin/annotation/processor/result"));
        CompilationSubject.assertThat(compilation).succeeded();

        // Verify
        ImmutableList<JavaFileObject> generatedFiles = compilation.generatedFiles();

        Optional<JavaFileObject> generatedSourceFile = generatedFiles
            .stream()
            .filter(generatedFile -> generatedFile.getKind() == JavaFileObject.Kind.SOURCE)
            .findFirst();

        assertTrue(generatedSourceFile.isPresent());
        JavaFileObjectSubject.assertThat(generatedSourceFile.get()).hasSourceEquivalentTo(RESULT);
    }

    private static List<JavaFileObject> readJavaFilesFromFolder(String folderPath) throws IOException {
        List<JavaFileObject> javaFileObjects = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(Path.of(folderPath))) {
            paths
                .filter(Files::isRegularFile) // Only regular files
                .filter(path -> path.toString().endsWith(".java")) // Filter for .java files
                .forEach(path -> {
                    // Add the file to the list as a JavaFileObject
                    try {
                        javaFileObjects.add(
                            JavaFileObjects.forSourceString(
                                path.toString().replace("src/test/java/", "").replace(".java", "").replace("/", "."),
                                readJavaCodeFromFile(path.toString())
                            )
                        );
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        }
        return javaFileObjects;
    }

    private static String readJavaCodeFromFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
