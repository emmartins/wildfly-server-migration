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

import org.jboss.dmr.ModelNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emmartins
 */
public class PermissionSetMapping {
    private List<String> principals;
    private List<PermissionSet> permissionSets;
    private Boolean matchAll;

    public PermissionSetMapping() {
        this.principals = new ArrayList<>();
        this.permissionSets = new ArrayList<>();
    }

    public PermissionSetMapping matchAll(boolean matchAll) {
        this.matchAll = matchAll;
        return this;
    }

    public PermissionSetMapping addPrincipal(String principal) {
        this.principals.add(principal);
        return this;
    }

    public PermissionSetMapping addPermissionSet(PermissionSet permissionSet) {
        this.permissionSets.add(permissionSet);
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
        if (permissionSets != null && !permissionSets.isEmpty()) {
            final ModelNode permissionSetsNode = modelNode.get("permission-sets").setEmptyList();
            for (PermissionSet permissionSet : permissionSets) {
                permissionSetsNode.add(permissionSet.toModelNode());
            }
        }
        return modelNode;
    }
}
