/*
 * Copyright 2017 Red Hat, Inc.
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

package org.jboss.migration.eap.task.subsystem.elytron;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emmartins
 */
public class ConstantPermissionMapperAddOperation {
    private final PathAddress subsystemPathAddress;
    private final String contanstPermissionMapper;
    private List<Permission> permissions;

    public ConstantPermissionMapperAddOperation(PathAddress subsystemPathAddress, String contanstPermissionMapper) {
        this.subsystemPathAddress = subsystemPathAddress;
        this.contanstPermissionMapper = contanstPermissionMapper;
        this.permissions = new ArrayList<>();
    }

    public ConstantPermissionMapperAddOperation addPermission(Permission permission) {
        this.permissions.add(permission);
        return this;
    }

    public ModelNode toModelNode() {
        /*
            "permissions" => [
                {"class-name" => "org.wildfly.security.auth.permission.LoginPermission"},
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
                ("constant-permission-mapper" => "constant-permission-mapper")
            ]
             */
        final PathAddress pathAddress = subsystemPathAddress.append("constant-permission-mapper", contanstPermissionMapper);
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
