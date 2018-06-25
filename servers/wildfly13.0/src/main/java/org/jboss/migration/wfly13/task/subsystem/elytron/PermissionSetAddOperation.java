/*
 * Copyright 2018 Red Hat, Inc.
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

package org.jboss.migration.wfly13.task.subsystem.elytron;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.wfly11.task.subsystem.elytron.Permission;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emmartins
 */
public class PermissionSetAddOperation {
    private final PathAddress subsystemPathAddress;
    private final String permissionSet;
    private List<Permission> permissions;

    public PermissionSetAddOperation(PathAddress subsystemPathAddress, String permissionSet) {
        this.subsystemPathAddress = subsystemPathAddress;
        this.permissionSet = permissionSet;
        this.permissions = new ArrayList<>();
    }

    public PermissionSetAddOperation addPermission(Permission permission) {
        this.permissions.add(permission);
        return this;
    }

    public ModelNode toModelNode() {
        /*
            "permissions" => [
                {
                    "class-name" => "org.wildfly.extension.batch.jberet.deployment.BatchPermission",
                    "module" => "org.wildfly.extension.batch.jberet",
                    "target-name" => "*"
                },
                {
                    "class-name" => "org.wildfly.transaction.client.RemoteTransactionPermission",
                    "module" => "org.wildfly.transaction.client"
                },
                {
                    "class-name" => "org.jboss.ejb.client.RemoteEJBPermission",
                    "module" => "org.jboss.ejb-client"
                }
            ],
            "operation" => "add",
            "address" => [
                ("subsystem" => "elytron"),
                ("permission-set" => "default-permissions")
            ]
             */
        final PathAddress pathAddress = subsystemPathAddress.append("permission-set", permissionSet);
        final ModelNode operation = Util.createAddOperation(pathAddress);
        if (permissions != null && !permissions.isEmpty()) {
            final ModelNode permissionsNode = operation.get("permissions").setEmptyList();
            for (Permission permission : permissions) {
                permissionsNode.add(permission.toModelNode());
            }
        }
        return operation;
    }
}
