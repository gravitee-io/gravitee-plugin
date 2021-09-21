package io.gravitee.plugin.connector;

import io.gravitee.connector.api.ConnectorFactory;
import io.gravitee.plugin.core.api.ConfigurablePluginManager;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface ConnectorPluginManager extends ConfigurablePluginManager<ConnectorPlugin> {

    ConnectorFactory<?> getConnector(String type);
}
