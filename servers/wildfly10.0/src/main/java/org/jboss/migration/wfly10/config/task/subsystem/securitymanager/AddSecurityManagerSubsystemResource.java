/*
 * Copyright 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.migration.wfly10.config.task.subsystem.securitymanager;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.jboss.JBossSubsystemNames;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.wfly10.config.management.SubsystemResource;
import org.jboss.migration.wfly10.config.task.management.resource.ManageableResourceBuildParameters;
import org.jboss.migration.wfly10.config.task.management.subsystem.AddSubsystemResourceSubtaskBuilder;

/**
 * A task which adds the default Security Manager subsystem, if missing from the server config.
 * @author emmartins
 */
public class AddSecurityManagerSubsystemResource<S> extends AddSubsystemResourceSubtaskBuilder<S> {

    protected AddSecurityManagerSubsystemResource() {
        super(JBossSubsystemNames.SECURITY_MANAGER);
    }

    private static final String DEPLOYMENT_PERMISSIONS = "deployment-permissions";
    private static final String DEPLOYMENT_PERMISSIONS_NAME = "default";
    private static final String MAXIMUM_PERMISSIONS = "maximum-permissions";
    private static final String CLASS_ATTR_NAME = "class";
    private static final String CLASS_ATTR_VALUE = "java.security.AllPermission";

    @Override
    protected void addConfiguration(ManageableResourceBuildParameters<S, SubsystemResource.Parent> params, TaskContext taskContext) {
        // reuse default empty config add from super
        super.addConfiguration(params, taskContext);
        // and add default permissions
            /*
            <subsystem xmlns="urn:jboss:domain:security-manager:1.0">
                <deployment-permissions>
                    <maximum-set>
                        <permission class="java.security.AllPermission"/>
                    </maximum-set>
                </deployment-permissions>
            </subsystem>
             */

        final SubsystemResource.Parent parentResource = params.getResource();
        final PathAddress subsystemPathAddress = parentResource.getSubsystemResourcePathAddress(getSubsystem());
        final PathAddress deploymentPermissionsPathAddress = subsystemPathAddress.append(DEPLOYMENT_PERMISSIONS, DEPLOYMENT_PERMISSIONS_NAME);
        final ModelNode deploymentPermissionsAddOperation = Util.createAddOperation(deploymentPermissionsPathAddress);
        final ModelNode maximumPermissions = new ModelNode();
        maximumPermissions.get(CLASS_ATTR_NAME).set(CLASS_ATTR_VALUE);
        deploymentPermissionsAddOperation.get(MAXIMUM_PERMISSIONS).add(maximumPermissions);
        parentResource.getServerConfiguration().executeManagementOperation(deploymentPermissionsAddOperation);
    }
}
