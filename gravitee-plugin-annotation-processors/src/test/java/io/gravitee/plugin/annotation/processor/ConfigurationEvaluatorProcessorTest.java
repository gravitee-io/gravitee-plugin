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
import java.util.Optional;
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

        JavaFileObject test = JavaFileObjects.forSourceString(
            "TestConfiguration",
            readJavaCodeFromFile("src/test/java/io/gravitee/plugin/annotation/processor/result/TestConfiguration.java")
        );

        // Run
        Compilation compilation = javac().withProcessors(processor).compile(test);
        CompilationSubject.assertThat(compilation).succeeded();

        // Verify
        ImmutableList<JavaFileObject> generatedFiles = compilation.generatedFiles();
        //assertEquals(11, generatedFiles.size());

        Optional<JavaFileObject> generatedSourceFile = generatedFiles
            .stream()
            .filter(generatedFile -> generatedFile.getKind() == JavaFileObject.Kind.SOURCE)
            .findFirst();

        assertTrue(generatedSourceFile.isPresent());
        JavaFileObjectSubject.assertThat(generatedSourceFile.get()).hasSourceEquivalentTo(RESULT);
    }

    private static String readJavaCodeFromFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
