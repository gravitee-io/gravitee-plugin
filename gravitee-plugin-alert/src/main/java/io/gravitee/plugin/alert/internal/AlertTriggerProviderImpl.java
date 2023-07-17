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

import io.gravitee.alert.api.trigger.AbstractTriggerProvider;
import io.gravitee.alert.api.trigger.Trigger;
import io.gravitee.plugin.alert.AlertTriggerProviderManager;
import org.springframework.beans.factory.annotation.Autowired;

public class AlertTriggerProviderImpl extends AbstractTriggerProvider {

    @Autowired
    private AlertTriggerProviderManager triggerProviderManager;

    @Override
    public void register(Trigger trigger) {
        triggerProviderManager.findAll().forEach(triggerProvider -> triggerProvider.register(trigger));
    }

    @Override
    public void unregister(Trigger trigger) {
        triggerProviderManager.findAll().forEach(triggerProvider -> triggerProvider.unregister(trigger));
    }

    @Override
    public void addListener(Listener listener) {
        triggerProviderManager.addListener(listener);
    }
}
