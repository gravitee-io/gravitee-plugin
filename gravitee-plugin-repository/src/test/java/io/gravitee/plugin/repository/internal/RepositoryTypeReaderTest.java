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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.gravitee.platform.repository.api.Scope;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

@RunWith(MockitoJUnitRunner.class)
public class RepositoryTypeReaderTest {

    @Mock
    private Environment environment;

    private final RepositoryTypeReader reader = new RepositoryTypeReader();

    @Test
    public void should_read_from_legacy_key_first() {
        when(environment.getProperty("management.type")).thenReturn("jdbc");
        String type = reader.getRepositoryType(environment, Scope.MANAGEMENT);

        assertEquals("jdbc", type);
    }

    @Test
    public void should_fallback_to_new_key_when_legacy_key_not_set() {
        when(environment.getProperty("management.type")).thenReturn(null);
        when(environment.getProperty("repositories.management.type")).thenReturn("mongodb");
        String type = reader.getRepositoryType(environment, Scope.MANAGEMENT);

        assertEquals("mongodb", type);
    }

    @Test
    public void should_return_null_when_properties_are_missing() {
        when(environment.getProperty("management.type")).thenReturn(null);
        when(environment.getProperty("repositories.management.type")).thenReturn(null);
        String type = reader.getRepositoryType(environment, Scope.MANAGEMENT);
        assertNull(type);
    }

    @Test
    public void should_log_warning_when_legacy_key_is_explicitly_defined() {
        // Use a real environment so isExplicitlyDefined can iterate property sources
        StandardEnvironment realEnv = new StandardEnvironment();
        realEnv.getPropertySources().addFirst(new MapPropertySource("test", Map.of("management.type", "jdbc")));

        Logger logger = (Logger) LoggerFactory.getLogger(RepositoryTypeReader.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        try {
            String type = reader.getRepositoryType(realEnv, Scope.MANAGEMENT);
            assertEquals("jdbc", type);
            assertEquals("Should have logged exactly one warning", 1, listAppender.list.size());

            ILoggingEvent logEvent = listAppender.list.get(0);
            assertEquals(Level.WARN, logEvent.getLevel());
            assertEquals(
                "Repository for scope 'MANAGEMENT' is configured using the deprecated key 'management.type'. Please migrate to 'repositories.management.type'.",
                logEvent.getFormattedMessage()
            );
        } finally {
            logger.detachAppender(listAppender);
        }
    }

    @Test
    public void should_not_log_warning_when_legacy_key_comes_from_alias() {
        StandardEnvironment realEnv = new StandardEnvironment();
        realEnv.getPropertySources().addFirst(new MapPropertySource("yaml", Map.of("repositories.management.type", "mongodb")));
        realEnv.getPropertySources().addFirst(new RepositoryAliasPropertySource(realEnv));

        Logger logger = (Logger) LoggerFactory.getLogger(RepositoryTypeReader.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        try {
            String type = reader.getRepositoryType(realEnv, Scope.MANAGEMENT);
            assertEquals("mongodb", type);
            assertEquals("Should not have logged any warning", 0, listAppender.list.size());
        } finally {
            logger.detachAppender(listAppender);
        }
    }
}
