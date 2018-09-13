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
package io.gravitee.plugin.alert.internal;

import io.gravitee.alert.api.event.Alertable;
import io.gravitee.alert.api.service.Alert;
import io.gravitee.alert.api.trigger.Trigger;
import io.gravitee.common.service.AbstractService;
import io.gravitee.node.api.Node;
import io.gravitee.plugin.alert.AlertEngineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Azize ELAMRANI (azize.elamrani at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AlertEngineServiceImpl extends AbstractService implements AlertEngineService {

    private final Logger logger = LoggerFactory.getLogger(AlertEngineServiceImpl.class);

    private final Collection<Alert> alerts = new ArrayList<>();

    @Autowired
    private Node node;
    @Value("${http.port:8082}")
    private String port;
    @Value("${tenant:#{null}}")
    private String tenant;

    @Override
    public void register(final Alert alert) {
        alerts.add(alert);
    }

    @Override
    public CompletableFuture<Void> send(final Alertable alertable) {
        final Map<String, Object> context = new LinkedHashMap<>(4);
        context.put("Gateway", node.id());
        context.put("Hostname", node.hostname());
        context.put("Port", port);
        if (tenant != null) {
            context.put("Tenant", tenant);
        }
        alertable.setContext(context);
        return CompletableFuture.allOf(alerts.stream()
                .filter(alert -> alert.canHandle(alertable))
                .map(alert -> alert.send(alertable))
                .toArray(CompletableFuture[]::new));
    }

    @Override
    public CompletableFuture<Void> send(Trigger trigger) {
        return CompletableFuture.allOf(alerts.stream()
                .filter(alert -> alert.canHandle(trigger))
                .map(alert -> alert.send(trigger))
                .toArray(CompletableFuture[]::new));
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        if (!alerts.isEmpty()) {
            for (final Alert alert : alerts) {
                try {
                    alert.start();
                } catch (Exception ex) {
                    logger.error("Unexpected error while starting alert", ex);
                }
            }
        } else {
            logger.info("\tThere is no alert to start");
        }
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();

        for(final Alert alert : alerts) {
            try {
                alert.stop();
            } catch (Exception ex) {
                logger.error("Unexpected error while stopping alert", ex);
            }
        }
    }

    @Override
    protected String name() {
        return "Alert Engine service";
    }
}
