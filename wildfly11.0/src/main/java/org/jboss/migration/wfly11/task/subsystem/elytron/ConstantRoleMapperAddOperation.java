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

package org.jboss.migration.wfly11.task.subsystem.elytron;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author emmartins
 */
public class ConstantRoleMapperAddOperation {
    private final PathAddress subsystemPathAddress;
    private final String constantRoleMapper;
    private List<String> roles;

    public ConstantRoleMapperAddOperation(PathAddress subsystemPathAddress, String constantRoleMapper) {
        this.subsystemPathAddress = subsystemPathAddress;
        this.constantRoleMapper = constantRoleMapper;
        this.roles = new ArrayList<>();
    }

    public ConstantRoleMapperAddOperation addRole(String role) {
        this.roles.add(role);
        return this;
    }

    public ModelNode toModelNode() {
            /*
              "roles" => ["SuperUser"],
            "operation" => "add",
            "address" => [
                ("subsystem" => "elytron"),
                ("constant-role-mapper" => "super-user-mapper")
            ]
             */
        final PathAddress pathAddress = subsystemPathAddress.append("constant-role-mapper", constantRoleMapper);
        final ModelNode operation = Util.createAddOperation(pathAddress);
        if (roles != null && !roles.isEmpty()) {
            final ModelNode rolesNode = operation.get("roles").setEmptyList();
            for (String role : roles) {
                rolesNode.add(role);
            }
        }
        return operation;
    }
}
