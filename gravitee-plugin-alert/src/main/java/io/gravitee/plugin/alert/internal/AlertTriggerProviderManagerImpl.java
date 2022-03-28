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

import io.gravitee.alert.api.trigger.TriggerProvider;
import io.gravitee.common.service.AbstractService;
import io.gravitee.plugin.alert.AlertTriggerProviderManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Azize ELAMRANI (azize.elamrani at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AlertTriggerProviderManagerImpl extends AbstractService implements AlertTriggerProviderManager {

    private final Logger logger = LoggerFactory.getLogger(AlertTriggerProviderManagerImpl.class);

    private final Collection<TriggerProvider> triggerProviders = new ArrayList<>();

    private final List<TriggerProvider.Listener> listeners = new ArrayList<>();

    @Override
    public void register(final TriggerProvider triggerProvider) {
        triggerProviders.add(triggerProvider);
    }

    @Override
    public Collection<TriggerProvider> findAll() {
        return triggerProviders;
    }

    @Override
    public void addListener(TriggerProvider.Listener listener) {
        listeners.add(listener);
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        if (!triggerProviders.isEmpty()) {
            for (final TriggerProvider provider : triggerProviders) {
                try {
                    listeners.forEach(provider::addListener);

                    provider.start();
                } catch (Exception ex) {
                    logger.error("Unexpected error while starting a trigger provider", ex);
                }
            }
        } else {
            logger.info("\tThere is no trigger provider to start");
        }
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();

        for (final TriggerProvider provider : triggerProviders) {
            try {
                provider.stop();
            } catch (Exception ex) {
                logger.error("Unexpected error while stopping trigger provider: {}", provider, ex);
            }
        }
    }

    @Override
    protected String name() {
        return "Alert Engine - Trigger provider manager";
    }
}
