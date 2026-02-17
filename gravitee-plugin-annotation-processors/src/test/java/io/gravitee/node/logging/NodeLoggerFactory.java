package io.gravitee.node.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NodeLoggerFactory initially comes from gravitee-node:gravitee-node-logging.
 * This class is used in the mustache templates, and we need it for compilation and test assertions purpose.
 * However, importing the Maven dependency would create a circular reference with gravitee-node, and that is not a good solution, that's why, we simply use this fake one.
 */
public class NodeLoggerFactory {

    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
}
