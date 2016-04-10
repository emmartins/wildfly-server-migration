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
package org.wildfly.migration.wfly10.subsystem.ejb3;

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
 * A task which defines EJB3 subsystem's 'passivation-disabled-cache-ref' attribute .
 * @author emmartins
 */
public class DefinePassivationDisabledCacheRef implements WildFly10SubsystemMigrationTask {

    public static final DefinePassivationDisabledCacheRef INSTANCE = new DefinePassivationDisabledCacheRef();

    private DefinePassivationDisabledCacheRef() {
    }

    private static final String DEFAULT_SFSB_CACHE_ATTR_NAME = "default-sfsb-cache";
    private static final String DEFAULT_SFSB_PASSIVATION_DISABLED_CACHE_ATTR_NAME = "default-sfsb-passivation-disabled-cache";

    @Override
    public void execute(ModelNode config, WildFly10Subsystem subsystem, WildFly10StandaloneServer server, ServerMigrationContext context) throws IOException {
        if (config == null || !config.hasDefined(DEFAULT_SFSB_CACHE_ATTR_NAME) || config.hasDefined(DEFAULT_SFSB_PASSIVATION_DISABLED_CACHE_ATTR_NAME)) {
            return;
        }
        // /subsystem=ejb3:write-attribute(name=default-sfsb-passivation-disabled-cache,value=defaultSFSBCache)
        final String defaultSFSBCache = config.get(DEFAULT_SFSB_CACHE_ATTR_NAME).asString();
        final ModelNode op = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, pathAddress(pathElement(SUBSYSTEM, subsystem.getName())));
        op.get(NAME).set(DEFAULT_SFSB_PASSIVATION_DISABLED_CACHE_ATTR_NAME);
        op.get(VALUE).set(defaultSFSBCache);
        server.executeManagementOperation(op);
        ServerMigrationLogger.ROOT_LOGGER.infof("EJB3 subsystem's 'default-sfsb-passivation-disabled-cache' attribute set to %s.", defaultSFSBCache);
    }
}
