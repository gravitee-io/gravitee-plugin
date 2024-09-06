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
package io.gravitee.plugin.repository.internal;

import io.gravitee.platform.repository.api.Scope;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

@RunWith(MockitoJUnitRunner.class)
public class RepositoryTypeReaderTest {

    @Mock
    Environment environment;

    RepositoryTypeReader reader = new RepositoryTypeReader();

    @Test
    public void scope_should_been_read_from_repositories_section_first() {
        Mockito.when(environment.getProperty("repositories.management.type")).thenReturn("mongodb");

        String type = reader.getRepositoryType(environment, Scope.MANAGEMENT);
        Assert.assertEquals("mongodb", type);
    }

    @Test
    public void scope_should_been_read_from_old_structure_as_fallback() {
        Mockito.when(environment.getProperty("repositories.management.type")).thenReturn(null);
        Mockito.when(environment.getProperty("management.type")).thenReturn("jdbc");

        String type = reader.getRepositoryType(environment, Scope.MANAGEMENT);
        Assert.assertEquals("jdbc", type);
    }

    @Test
    public void should_return_null_when_properties_are_missing() {
        Mockito.when(environment.getProperty("repositories.management.type")).thenReturn(null);
        Mockito.when(environment.getProperty("management.type")).thenReturn(null);

        String type = reader.getRepositoryType(environment, Scope.MANAGEMENT);
        Assert.assertNull(type);
    }
}
