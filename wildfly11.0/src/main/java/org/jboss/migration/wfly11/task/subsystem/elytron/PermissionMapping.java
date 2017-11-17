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

import org.jboss.dmr.ModelNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emmartins
 */
public class PermissionMapping {
    private List<String> principals;
    private List<Permission> permissions;
    private Boolean matchAll;

    public PermissionMapping() {
        this.principals = new ArrayList<>();
        this.permissions = new ArrayList<>();
    }

    public PermissionMapping matchAll(boolean matchAll) {
        this.matchAll = matchAll;
        return this;
    }

    public PermissionMapping addPrincipal(String principal) {
        this.principals.add(principal);
        return this;
    }

    public PermissionMapping addPermission(Permission permission) {
        this.permissions.add(permission);
        return this;
    }

    public ModelNode toModelNode() {
        final ModelNode modelNode = new ModelNode();
        if (matchAll != null) {
            modelNode.get("match-all").set(matchAll);
        }
        if (principals != null && !principals.isEmpty()) {
            final ModelNode principalsNode = modelNode.get("principals").setEmptyList();
            for (String principal : principals) {
                principalsNode.add(principal);
            }
        }
        if (permissions != null && !permissions.isEmpty()) {
            final ModelNode permissionsNode = modelNode.get("permissions").setEmptyList();
            for (Permission permission : permissions) {
                permissionsNode.add(permission.toModelNode());
            }
        }
        return modelNode;
    }
}
