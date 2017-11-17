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
public class MechanismProviderFilteringSaslServerFactoryAddOperation {
    private final PathAddress subsystemPathAddress;
    private final String mechanismProviderFilteringSaslServerFactory;
    private String saslServerFactory;
    private List<String> filters;

    public MechanismProviderFilteringSaslServerFactoryAddOperation(PathAddress subsystemPathAddress, String mechanismProviderFilteringSaslServerFactory) {
        this.subsystemPathAddress = subsystemPathAddress;
        this.mechanismProviderFilteringSaslServerFactory = mechanismProviderFilteringSaslServerFactory;
        this.filters = new ArrayList<>();
    }

    public MechanismProviderFilteringSaslServerFactoryAddOperation saslServerFactory(String saslServerFactory) {
        this.saslServerFactory = saslServerFactory;
        return this;
    }

    public MechanismProviderFilteringSaslServerFactoryAddOperation addFilter(String filter) {
        this.filters.add(filter);
        return this;
    }

    public ModelNode toModelNode() {
            /*
               "sasl-server-factory" => "global",
            "filters" => [{"provider-name" => "WildFlyElytron"}],
            "operation" => "add",
            "address" => [
                ("subsystem" => "elytron"),
                ("mechanism-provider-filtering-sasl-server-factory" => "elytron")
            ]
             */
        final PathAddress pathAddress = subsystemPathAddress.append("mechanism-provider-filtering-sasl-server-factory", mechanismProviderFilteringSaslServerFactory);
        final ModelNode operation = Util.createAddOperation(pathAddress);
        if (saslServerFactory != null) {
            operation.get("sasl-server-factory").set(saslServerFactory);
        }
        if (filters != null && !filters.isEmpty()) {
            final ModelNode filtersNode = operation.get("filters").setEmptyList();
            for (String filter : filters) {
                filtersNode.add("provider-name",filter);
            }
        }
        return operation;
    }
}
