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

/**
 * @author emmartins
 */
public class Permission {
    private final String className;
    private String module;
    private String targetName;

    public Permission(String className) {
        this.className = className;
    }

    public Permission module(String module) {
        this.module = module;
        return this;
    }

    public Permission targetName(String targetName) {
        this.targetName = targetName;
        return this;
    }

    ModelNode toModelNode() {
        final ModelNode modelNode = new ModelNode();
        modelNode.get("class-name").set(className);
        if (module != null) {
            modelNode.get("module").set(module);
        }
        if (targetName != null) {
            modelNode.get("target-name").set(targetName);
        }
        return modelNode;
    }
}
