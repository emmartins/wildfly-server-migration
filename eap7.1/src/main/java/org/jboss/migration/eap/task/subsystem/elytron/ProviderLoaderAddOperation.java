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

/**
 * @author emmartins
 */
public class ProviderLoaderAddOperation {
    private final PathAddress subsystemPathAddress;
    private final String providerLoader;
    private String module;

    public ProviderLoaderAddOperation(PathAddress subsystemPathAddress, String providerLoader) {
        this.subsystemPathAddress = subsystemPathAddress;
        this.providerLoader = providerLoader;
    }

    public ProviderLoaderAddOperation module(String module) {
        this.module = module;
        return this;
    }

    public ModelNode toModelNode() {
        /*
            "module" => "org.wildfly.security.elytron",
            "operation" => "add",
            "address" => [
                ("subsystem" => "elytron"),
                ("provider-loader" => "elytron")
            ]
             */
        final PathAddress pathAddress = subsystemPathAddress.append("provider-loader", providerLoader);
        final ModelNode operation = Util.createAddOperation(pathAddress);
        if (module != null) {
            operation.get("module").set(module);
        }
        return operation;
    }
}
