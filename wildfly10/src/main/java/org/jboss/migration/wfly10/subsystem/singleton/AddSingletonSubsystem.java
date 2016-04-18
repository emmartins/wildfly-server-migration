/*
 * Copyright 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.migration.wfly10.subsystem.singleton;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ServerMigrationContext;
import org.jboss.migration.core.logger.ServerMigrationLogger;
import org.jboss.migration.wfly10.standalone.WildFly10StandaloneServer;
import org.jboss.migration.wfly10.subsystem.WildFly10Subsystem;
import org.jboss.migration.wfly10.subsystem.WildFly10SubsystemMigrationTask;

import java.io.IOException;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * A task which adds the default Singleton subsystem, if missing from the server config.
 * @author emmartins
 */
public class AddSingletonSubsystem implements WildFly10SubsystemMigrationTask {

    public static final AddSingletonSubsystem INSTANCE = new AddSingletonSubsystem();

    private AddSingletonSubsystem() {
    }

    private static final String DEFAULT_ATTR_NAME = "default";
    private static final String DEFAULT_ATTR_VALUE = "default";

    private static final String SINGLETON_POLICY = "singleton-policy";
    private static final String CACHE_CONTAINER_ATTR_NAME = "cache-container";
    private static final String CACHE_CONTAINER_ATTR_VALUE = "server";

    private static final String ELECTION_POLICY = "election-policy";
    private static final String ELECTION_POLICY_NAME = "simple";

    @Override
    public void execute(ModelNode config, WildFly10Subsystem subsystem, WildFly10StandaloneServer server, ServerMigrationContext context) throws IOException {
        if (config != null) {
            return;
        }
        ServerMigrationLogger.ROOT_LOGGER.debugf("Adding subsystem %s...", subsystem.getName());
        // add subsystem with default config
            /*
            <subsystem xmlns="urn:jboss:domain:singleton:1.0">
            <singleton-policies default="default">
                <singleton-policy name="default" cache-container="server">
                    <simple-election-policy/>
                </singleton-policy>
            </singleton-policies>
        </subsystem>
             */
        final Operations.CompositeOperationBuilder compositeOperationBuilder = Operations.CompositeOperationBuilder.create();
        final PathAddress subsystemPathAddress = pathAddress(pathElement(SUBSYSTEM, subsystem.getName()));
        final ModelNode subsystemAddOperation = Util.createAddOperation(subsystemPathAddress);
        subsystemAddOperation.get(DEFAULT_ATTR_NAME).set(DEFAULT_ATTR_VALUE);
        compositeOperationBuilder.addStep(subsystemAddOperation);
        // add default policy
        final PathAddress singletonPolicyPathAddress = subsystemPathAddress.append(SINGLETON_POLICY, DEFAULT_ATTR_VALUE);
        final ModelNode singletonPolicyAddOperation = Util.createAddOperation(singletonPolicyPathAddress);
        singletonPolicyAddOperation.get(CACHE_CONTAINER_ATTR_NAME).set(CACHE_CONTAINER_ATTR_VALUE);
        compositeOperationBuilder.addStep(singletonPolicyAddOperation);
        // add election policy
        final PathAddress electionPolicyPathAddress = singletonPolicyPathAddress.append(ELECTION_POLICY, ELECTION_POLICY_NAME);
        final ModelNode electionPolicyAddOperation = Util.createAddOperation(electionPolicyPathAddress);
        compositeOperationBuilder.addStep(electionPolicyAddOperation);
        server.executeManagementOperation(compositeOperationBuilder.build().getOperation());
        ServerMigrationLogger.ROOT_LOGGER.infof("Subsystem %s added.", subsystem.getName());
    }
}
