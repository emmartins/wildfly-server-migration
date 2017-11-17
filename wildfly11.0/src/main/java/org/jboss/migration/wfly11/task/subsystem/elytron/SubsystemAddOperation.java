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
public class SubsystemAddOperation {
    private final PathAddress subsystemPathAddress;
    private String finalProviders;
    private List<String> disallowedProviders;

    public SubsystemAddOperation(PathAddress subsystemPathAddress) {
        this.subsystemPathAddress = subsystemPathAddress;
        this.disallowedProviders = new ArrayList<>();
    }

    public SubsystemAddOperation finalProviders(String finalProviders) {
        this.finalProviders = finalProviders;
        return this;
    }

    public SubsystemAddOperation addDisallowedProvider(String disallowedProvider) {
        this.disallowedProviders.add(disallowedProvider);
        return this;
    }

    public ModelNode toModelNode() {
            /*
            /*
            "final-providers" => "combined-providers",
            "disallowed-providers" => ["OracleUcrypto"],
            "operation" => "add",
            "address" => [("subsystem" => "elytron")]
             */
        final ModelNode operation = Util.createAddOperation(subsystemPathAddress);
        if (finalProviders != null) {
            operation.get("final-providers").set(finalProviders);
        }
        if (disallowedProviders != null && !disallowedProviders.isEmpty()) {
            final ModelNode disallowedProvidersNode = operation.get("disallowed-providers").setEmptyList();
            for (String disallowedProvider : disallowedProviders) {
                disallowedProvidersNode.add(disallowedProvider);
            }
        }
        return operation;
    }
}
