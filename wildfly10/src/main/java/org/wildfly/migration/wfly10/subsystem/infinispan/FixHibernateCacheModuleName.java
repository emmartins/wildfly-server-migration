/*
 * Copyright 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.migration.wfly10.subsystem.infinispan;

import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.wildfly.migration.core.ServerMigrationContext;
import org.wildfly.migration.core.logger.ServerMigrationLogger;
import org.wildfly.migration.wfly10.standalone.WildFly10StandaloneServer;
import org.wildfly.migration.wfly10.subsystem.WildFly10Subsystem;
import org.wildfly.migration.wfly10.subsystem.WildFly10SubsystemMigrationTask;

import java.io.IOException;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * A task which changes the module name of any Hibernate Cache present in Infinispan subsystem.
 * @author emmartins
 */
public class FixHibernateCacheModuleName implements WildFly10SubsystemMigrationTask {

    public static final FixHibernateCacheModuleName INSTANCE = new FixHibernateCacheModuleName();

    private FixHibernateCacheModuleName() {
    }

    private static final String CACHE_CONTAINER = "cache-container";
    private static final String MODULE_ATTR_NAME = "module";
    private static final String MODULE_ATTR_OLD_VALUE_EAP6 = "org.jboss.as.jpa.hibernate:4";
    private static final String MODULE_ATTR_OLD_VALUE_WFLY8 = "org.hibernate";
    private static final String MODULE_ATTR_NEW_VALUE = "org.hibernate.infinispan";

    @Override
    public void execute(ModelNode config, WildFly10Subsystem subsystem, WildFly10StandaloneServer server, ServerMigrationContext context) throws IOException {
        if (config == null) {
            return;
        }
        if (!config.hasDefined(CACHE_CONTAINER)) {
            ServerMigrationLogger.ROOT_LOGGER.infof("No Cache container");
            return;
        }
        for (String cacheName : config.get(CACHE_CONTAINER).keys()) {
            final ModelNode cache = config.get(CACHE_CONTAINER, cacheName);
            if (cache.hasDefined(MODULE_ATTR_NAME) && (cache.get(MODULE_ATTR_NAME).asString().equals(MODULE_ATTR_OLD_VALUE_EAP6) || cache.get(MODULE_ATTR_NAME).asString().equals(MODULE_ATTR_OLD_VALUE_WFLY8))) {
                // /subsystem=infinispan/cache-container=cacheName:write-attribute(name=MODULE_ATTR_NAME,value=MODULE_ATTR_NEW_VALUE)
                final ModelNode op = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress(pathElement(SUBSYSTEM, subsystem.getName()), pathElement(CACHE_CONTAINER, cacheName)));
                op.get(NAME).set(MODULE_ATTR_NAME);
                op.get(VALUE).set(MODULE_ATTR_NEW_VALUE);
                server.executeManagementOperation(op);
                ServerMigrationLogger.ROOT_LOGGER.infof("Infinispan subsystem's cache %s 'module' attribute updated to %s.", cacheName, MODULE_ATTR_NEW_VALUE);
            }
        }
    }
}
