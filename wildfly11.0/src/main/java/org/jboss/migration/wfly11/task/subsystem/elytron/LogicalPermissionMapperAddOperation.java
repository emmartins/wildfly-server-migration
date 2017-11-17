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

/**
 *
 * @author emmartins
 */
public class LogicalPermissionMapperAddOperation {
    private final PathAddress subsystemPathAddress;
    private final String logicalPermissionMapper;
    private String left;
    private String right;
    private String logicalOperation;

    public LogicalPermissionMapperAddOperation(PathAddress subsystemPathAddress, String logicalPermissionMapper) {
        this.subsystemPathAddress = subsystemPathAddress;
        this.logicalPermissionMapper = logicalPermissionMapper;
    }

    public LogicalPermissionMapperAddOperation left(String left) {
        this.left = left;
        return this;
    }

    public LogicalPermissionMapperAddOperation right(String right) {
        this.right = right;
        return this;
    }

    public LogicalPermissionMapperAddOperation logicalOperation(String logicalOperation) {
        this.logicalOperation = logicalOperation;
        return this;
    }

    public ModelNode toModelNode() {
        /*
            "logical-operation" => "unless",
            "left" => "constant-permission-mapper",
            "right" => "anonymous-permission-mapper",
            "operation" => "add",
            "address" => [
                ("subsystem" => "elytron"),
                ("logical-permission-mapper" => "default-permission-mapper")
            ]
             */
        final PathAddress pathAddress = subsystemPathAddress.append("logical-permission-mapper", logicalPermissionMapper);
        final ModelNode operation = Util.createAddOperation(pathAddress);
        if (logicalOperation != null) {
            operation.get("logical-operation").set(logicalOperation);
        }
        if (left != null) {
            operation.get("left").set(left);
        }
        if (right != null) {
            operation.get("right").set(right);
        }
        return operation;
    }
}
