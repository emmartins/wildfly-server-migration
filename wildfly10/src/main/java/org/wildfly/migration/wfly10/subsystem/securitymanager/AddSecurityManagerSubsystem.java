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
package org.wildfly.migration.wfly10.subsystem.securitymanager;

import org.jboss.as.controller.PathAddress;
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
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * A task which adds the default Security Manager subsystem, if missing from the server config.
 * @author emmartins
 */
public class AddSecurityManagerSubsystem implements WildFly10SubsystemMigrationTask {

    public static final AddSecurityManagerSubsystem INSTANCE = new AddSecurityManagerSubsystem();

    private AddSecurityManagerSubsystem() {
    }

    private static final String DEPLOYMENT_PERMISSIONS = "deployment-permissions";
    private static final String DEPLOYMENT_PERMISSIONS_NAME = "default";
    private static final String MAXIMUM_PERMISSIONS = "maximum-permissions";
    private static final String CLASS_ATTR_NAME = "class";
    private static final String CLASS_ATTR_VALUE = "java.security.AllPermission";

    @Override
    public void execute(ModelNode config, WildFly10Subsystem subsystem, WildFly10StandaloneServer server, ServerMigrationContext context) throws IOException {
        if (config != null) {
            return;
        }
        ServerMigrationLogger.ROOT_LOGGER.debugf("Adding subsystem %s...", subsystem.getName());
        // add subsystem with default config
            /*
            <subsystem xmlns="urn:jboss:domain:security-manager:1.0">
                <deployment-permissions>
                    <maximum-set>
                        <permission class="java.security.AllPermission"/>
                    </maximum-set>
                </deployment-permissions>
            </subsystem>
             */
        final PathAddress subsystemPathAddress = pathAddress(pathElement(SUBSYSTEM, subsystem.getName()));
        final ModelNode subsystemAddOperation = Util.createAddOperation(subsystemPathAddress);
        server.executeManagementOperation(subsystemAddOperation);
        // add default deployment permissions
        final PathAddress deploymentPermissionsPathAddress = subsystemPathAddress.append(DEPLOYMENT_PERMISSIONS, DEPLOYMENT_PERMISSIONS_NAME);
        final ModelNode deploymentPermissionsAddOperation = Util.createAddOperation(deploymentPermissionsPathAddress);
        final ModelNode maximumPermissions = new ModelNode();
        maximumPermissions.get(CLASS_ATTR_NAME).set(CLASS_ATTR_VALUE);
        deploymentPermissionsAddOperation.get(MAXIMUM_PERMISSIONS).add(maximumPermissions);
        server.executeManagementOperation(deploymentPermissionsAddOperation);
        ServerMigrationLogger.ROOT_LOGGER.infof("Subsystem %s added.", subsystem.getName());
    }
}
