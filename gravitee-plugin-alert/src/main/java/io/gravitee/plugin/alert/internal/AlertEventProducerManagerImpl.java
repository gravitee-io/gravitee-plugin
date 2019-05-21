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

import io.gravitee.alert.api.event.EventProducer;
import io.gravitee.common.service.AbstractService;
import io.gravitee.plugin.alert.AlertEventProducerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Azize ELAMRANI (azize.elamrani at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AlertEventProducerManagerImpl extends AbstractService implements AlertEventProducerManager {

    private final Logger logger = LoggerFactory.getLogger(AlertEventProducerManagerImpl.class);

    private final Collection<EventProducer> eventProducers = new ArrayList<>();

    @Override
    public void register(final EventProducer eventProducer) {
        eventProducers.add(eventProducer);
    }

    @Override
    public Collection<EventProducer> findAll() {
        return eventProducers;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        if (!eventProducers.isEmpty()) {
            for (final EventProducer producer : eventProducers) {
                try {
                    producer.start();
                } catch (Exception ex) {
                    logger.error("Unexpected error while starting an event producer", ex);
                }
            }
        } else {
            logger.info("\tThere is no event producer to start");
        }
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();

        for(final EventProducer producer : eventProducers) {
            try {
                producer.stop();
            } catch (Exception ex) {
                logger.error("Unexpected error while stopping event producer: {}", producer, ex);
            }
        }
    }

    @Override
    protected String name() {
        return "Alert Engine - Event producer";
    }
}
