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

import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

public class RepositoryAliasPropertySourceTest {

    private ConfigurableEnvironment environment;

    @Before
    public void setUp() {
        environment = new StandardEnvironment();
    }

    private void setProperties(Map<String, Object> props) {
        environment.getPropertySources().addFirst(new MapPropertySource("test", props));
        environment.getPropertySources().addFirst(new RepositoryAliasPropertySource(environment));
    }

    @Test
    public void should_resolve_management_mongodb_uri_from_new_prefix() {
        setProperties(Map.of("repositories.management.mongodb.uri", "mongodb://newhost:27017/gravitee"));

        assertEquals("mongodb://newhost:27017/gravitee", environment.getProperty("management.mongodb.uri"));
    }

    @Test
    public void should_resolve_analytics_elasticsearch_endpoint_from_new_prefix() {
        setProperties(Map.of("repositories.analytics.elasticsearch.endpoints[0]", "http://es-host:9200"));

        assertEquals("http://es-host:9200", environment.getProperty("analytics.elasticsearch.endpoints[0]"));
    }

    @Test
    public void should_resolve_ratelimit_redis_host_from_new_prefix() {
        setProperties(Map.of("repositories.ratelimit.redis.host", "redis-host"));

        assertEquals("redis-host", environment.getProperty("ratelimit.redis.host"));
    }

    @Test
    public void should_fallback_to_old_prefix_when_new_prefix_not_set() {
        setProperties(Map.of("management.mongodb.uri", "mongodb://oldhost:27017/gravitee"));

        assertEquals("mongodb://oldhost:27017/gravitee", environment.getProperty("management.mongodb.uri"));
    }

    @Test
    public void should_prefer_new_prefix_when_both_are_set() {
        setProperties(
            Map.of(
                "management.mongodb.uri",
                "mongodb://oldhost:27017/gravitee",
                "repositories.management.mongodb.uri",
                "mongodb://newhost:27017/gravitee"
            )
        );

        assertEquals("mongodb://newhost:27017/gravitee", environment.getProperty("management.mongodb.uri"));
    }

    @Test
    public void should_not_intercept_unrelated_properties() {
        setProperties(Map.of("server.port", "8080"));

        assertEquals("8080", environment.getProperty("server.port"));
    }

    @Test
    public void should_return_null_for_missing_property() {
        setProperties(Map.of());

        assertNull(environment.getProperty("management.mongodb.uri"));
    }

    @Test
    public void should_resolve_analytics_elasticsearch_security_username() {
        setProperties(Map.of("repositories.analytics.elasticsearch.security.username", "elastic_user"));

        assertEquals("elastic_user", environment.getProperty("analytics.elasticsearch.security.username"));
    }

    @Test
    public void should_resolve_management_jdbc_url_from_new_prefix() {
        setProperties(Map.of("repositories.management.jdbc.url", "jdbc:postgresql://pg-host/gravitee"));

        assertEquals("jdbc:postgresql://pg-host/gravitee", environment.getProperty("management.jdbc.url"));
    }

    @Test
    public void should_resolve_ratelimit_mongodb_uri_from_new_prefix() {
        setProperties(Map.of("repositories.ratelimit.mongodb.uri", "mongodb://rl-host:27017/gravitee"));

        assertEquals("mongodb://rl-host:27017/gravitee", environment.getProperty("ratelimit.mongodb.uri"));
    }

    @Test
    public void should_resolve_elasticsearch_index_from_new_prefix() {
        setProperties(Map.of("repositories.analytics.elasticsearch.index", "my-index"));

        assertEquals("my-index", environment.getProperty("analytics.elasticsearch.index"));
    }

    @Test
    public void should_resolve_multiple_elasticsearch_endpoints_from_new_prefix() {
        setProperties(
            Map.of(
                "repositories.analytics.elasticsearch.endpoints[0]",
                "http://es1:9200",
                "repositories.analytics.elasticsearch.endpoints[1]",
                "http://es2:9200"
            )
        );

        assertEquals("http://es1:9200", environment.getProperty("analytics.elasticsearch.endpoints[0]"));
        assertEquals("http://es2:9200", environment.getProperty("analytics.elasticsearch.endpoints[1]"));
    }
}
