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
 * @author emmartins
 */
public class ProviderSaslServerFactoryAddOperation {
    private final PathAddress subsystemPathAddress;
    private final String providerSaslServerFactory;

    public ProviderSaslServerFactoryAddOperation(PathAddress subsystemPathAddress, String providerSaslServerFactory) {
        this.subsystemPathAddress = subsystemPathAddress;
        this.providerSaslServerFactory = providerSaslServerFactory;
    }

    public ModelNode toModelNode() {
         /*
              "operation" => "add",
            "address" => [
                ("subsystem" => "elytron"),
                ("provider-sasl-server-factory" => "global")
            ]
             */
        final PathAddress pathAddress = subsystemPathAddress.append("provider-sasl-server-factory", providerSaslServerFactory);
        final ModelNode operation = Util.createAddOperation(pathAddress);
        return operation;
    }
}
