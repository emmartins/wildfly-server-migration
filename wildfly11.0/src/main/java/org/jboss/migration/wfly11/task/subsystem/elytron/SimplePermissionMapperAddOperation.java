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
 * @author emmartins
 */
public class SimplePermissionMapperAddOperation {
    private final PathAddress subsystemPathAddress;
    private final String simplePermissionMapper;
    private String mappingMode;
    private List<PermissionMapping> permissionMappings;

    public SimplePermissionMapperAddOperation(PathAddress subsystemPathAddress, String simplePermissionMapper) {
        this.subsystemPathAddress = subsystemPathAddress;
        this.simplePermissionMapper = simplePermissionMapper;
        this.permissionMappings = new ArrayList<>();
    }

    public SimplePermissionMapperAddOperation mappingMode(String mappingMode) {
        this.mappingMode = mappingMode;
        return this;
    }

    public SimplePermissionMapperAddOperation addPermissionMapping(PermissionMapping permissionMapping) {
        this.permissionMappings.add(permissionMapping);
        return this;
    }

    public ModelNode toModelNode() {
        /*
            "permission-mappings" => [{
                "principals" => ["anonymous"],
                "permissions" => [{"class-name" => "org.wildfly.security.auth.permission.LoginPermission"}]
            }],
            "operation" => "add",
            "address" => [
                ("subsystem" => "elytron"),
                ("simple-permission-mapper" => "anonymous-permission-mapper")
            ]
             */
        final PathAddress pathAddress = subsystemPathAddress.append("simple-permission-mapper", simplePermissionMapper);
        final ModelNode operation = Util.createAddOperation(pathAddress);
        if (mappingMode != null) {
            operation.get("mapping-mode").set(mappingMode);
        }
        if (permissionMappings != null && !permissionMappings.isEmpty()) {
            final ModelNode permissionMappingsNode = operation.get("permission-mappings").setEmptyList();
            for (PermissionMapping permissionMapping : permissionMappings) {
                permissionMappingsNode.add(permissionMapping.toModelNode());
            }
        }
        return operation;
    }
}
