/*
 * Copyright 2020 Red Hat, Inc.
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
package org.jboss.migration.eap.task.subsystem.jgroups;

import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.wfly10.config.task.subsystem.jgroups.UpdateProtocols;

/**
 * Jgroups update protocol's Operation to move timeout property from TCPPING to pbcast.GMS.
 * @author emmartins
 */
public class EAP7_2MoveTcpPingTimeoutOperation implements UpdateProtocols.Operation {

    @Override
    public void execute(UpdateProtocols.ProtocolStack protocolStack, TaskContext context) {
        final String pbcastGMS = "pbcast.GMS";
        final String tcpping = "org.jgroups.protocols.TCPPING";
        final ModelNode tcppingValue = protocolStack.get(tcpping);
        if (tcppingValue != null && tcppingValue.hasDefined("properties","timeout")) {
            final ModelNode properties = tcppingValue.get("properties");
            final ModelNode timeout = properties.remove("timeout").clone();
            protocolStack.update(tcpping, tcppingValue);
            final ModelNode pbcastGMSValue = protocolStack.get(pbcastGMS);
            if (pbcastGMSValue != null) {
                pbcastGMSValue.get("properties","join_timeout").set(timeout);
                protocolStack.update(pbcastGMS, pbcastGMSValue);
            }
        }
    }
}
