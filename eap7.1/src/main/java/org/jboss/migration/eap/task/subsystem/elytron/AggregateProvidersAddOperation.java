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
 *
 * @author emmartins
 */
public class AggregateProvidersAddOperation {
    private final PathAddress subsystemPathAddress;
    private final String aggregateProviders;
    private List<String> providers;

    public AggregateProvidersAddOperation(PathAddress subsystemPathAddress, String aggregateProviders) {
        this.subsystemPathAddress = subsystemPathAddress;
        this.aggregateProviders = aggregateProviders;
        this.providers = new ArrayList<>();
    }

    public AggregateProvidersAddOperation addProvider(String provider) {
        this.providers.add(provider);
        return this;
    }

    public ModelNode toModelNode() {
            /*
            "providers" => [
                "elytron",
                "openssl"
            ],
            "operation" => "add",
            "address" => [
                ("subsystem" => "elytron"),
                ("aggregate-providers" => "combined-providers")
            ]
             */
        final PathAddress pathAddress = subsystemPathAddress.append("aggregate-providers", aggregateProviders);
        final ModelNode operation = Util.createAddOperation(pathAddress);
        if (providers != null && !providers.isEmpty()) {
            final ModelNode providersNode = operation.get("providers").setEmptyList();
            for (String provider : providers) {
                providersNode.add(provider);
            }
        }
        return operation;
    }
}
