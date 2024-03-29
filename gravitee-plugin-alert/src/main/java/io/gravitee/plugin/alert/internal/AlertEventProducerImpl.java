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
package io.gravitee.plugin.alert.internal;

import io.gravitee.alert.api.event.AbstractEventProducer;
import io.gravitee.alert.api.event.Event;
import io.gravitee.plugin.alert.AlertEventProducer;
import io.gravitee.plugin.alert.AlertEventProducerManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AlertEventProducerImpl extends AbstractEventProducer implements AlertEventProducer {

    @Autowired
    private AlertEventProducerManager producerManager;

    @Override
    public void send(Event event) {
        producerManager.findAll().forEach(eventProducer -> eventProducer.send(event));
    }

    @Override
    public boolean isEmpty() {
        return producerManager.findAll().isEmpty();
    }
}
