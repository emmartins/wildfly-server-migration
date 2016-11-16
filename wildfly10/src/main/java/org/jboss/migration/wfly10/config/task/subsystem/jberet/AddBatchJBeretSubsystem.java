/*
 * Copyright 2016 Red Hat, Inc.
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
package org.jboss.migration.wfly10.config.task.subsystem.jberet;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.wfly10.config.management.SubsystemsManagement;
import org.jboss.migration.wfly10.config.task.subsystem.AddSubsystemConfigSubtask;
import org.jboss.migration.wfly10.config.task.subsystem.SubsystemNames;

/**
 * A task which adds the default Batch JBeret subsystem, if missing from the server config.
 * @author emmartins
 */
public class AddBatchJBeretSubsystem<S> extends AddSubsystemConfigSubtask<S> {

    public static final AddBatchJBeretSubsystem INSTANCE = new AddBatchJBeretSubsystem();

    private AddBatchJBeretSubsystem() {
        super(SubsystemNames.BATCH_JBERET);
    }

    private static final String DEFAULT_JOB_REPOSITORY_ATTR_NAME = "default-job-repository";
    private static final String DEFAULT_JOB_REPOSITORY_ATTR_VALUE = "in-memory";
    private static final String DEFAULT_THREAD_POOL_ATTR_NAME = "default-thread-pool";
    private static final String DEFAULT_THREAD_POOL_ATTR_VALUE = "batch";

    private static final String IN_MEMORY_JOB_REPOSITORY = "in-memory-job-repository";

    private static final String THREAD_POOL = "thread-pool";
    private static final String MAX_THREADS = "max-threads";
    private static final String MAX_THREADS_VALUE = "10";
    private static final String KEEPALIVE_TIME = "keepalive-time";
    private static final String KEEPALIVE_TIME_TIME_ATTR_NAME = "time";
    private static final String KEEPALIVE_TIME_TIME_ATTR_VALUE = "30";
    private static final String KEEPALIVE_TIME_UNIT_ATTR_NAME = "unit";
    private static final String KEEPALIVE_TIME_UNIT_ATTR_VALUE = "seconds";

    @Override
    protected void addSubsystem(SubsystemsManagement subsystemsManagement, ServerMigrationTaskContext context) throws Exception {
        /*
            <subsystem xmlns="urn:jboss:domain:batch-jberet:1.0">
                <default-job-repository name="in-memory"/>
                <default-thread-pool name="batch"/>
                <job-repository name="in-memory">
                    <in-memory/>
                </job-repository>
                <thread-pool name="batch">
                    <max-threads count="10"/>
                    <keepalive-time time="30" unit="seconds"/>
                </thread-pool>
            </subsystem>
             */
        final Operations.CompositeOperationBuilder compositeOperationBuilder = Operations.CompositeOperationBuilder.create();
        final PathAddress subsystemPathAddress = subsystemsManagement.getResourcePathAddress(subsystemName);
        final ModelNode subsystemAddOperation = Util.createAddOperation(subsystemPathAddress);
        subsystemAddOperation.get(DEFAULT_JOB_REPOSITORY_ATTR_NAME).set(DEFAULT_JOB_REPOSITORY_ATTR_VALUE);
        subsystemAddOperation.get(DEFAULT_THREAD_POOL_ATTR_NAME).set(DEFAULT_THREAD_POOL_ATTR_VALUE);
        compositeOperationBuilder.addStep(subsystemAddOperation);
        // add default job repository
        final PathAddress jobReporsitoryPathAddress = subsystemPathAddress.append(IN_MEMORY_JOB_REPOSITORY, DEFAULT_JOB_REPOSITORY_ATTR_VALUE);
        final ModelNode jobReporsitoryAddOperation = Util.createAddOperation(jobReporsitoryPathAddress);
        compositeOperationBuilder.addStep(jobReporsitoryAddOperation);
        // add default thread pool
        final PathAddress threadPoolPathAddress = subsystemPathAddress.append(THREAD_POOL, DEFAULT_THREAD_POOL_ATTR_VALUE);
        final ModelNode threadPoolAddOperation = Util.createAddOperation(threadPoolPathAddress);
        threadPoolAddOperation.get(MAX_THREADS).set(MAX_THREADS_VALUE);
        final ModelNode keepAliveTime = new ModelNode();
        keepAliveTime.get(KEEPALIVE_TIME_TIME_ATTR_NAME).set(KEEPALIVE_TIME_TIME_ATTR_VALUE);
        keepAliveTime.get(KEEPALIVE_TIME_UNIT_ATTR_NAME).set(KEEPALIVE_TIME_UNIT_ATTR_VALUE);
        threadPoolAddOperation.get(KEEPALIVE_TIME).set(keepAliveTime);
        compositeOperationBuilder.addStep(threadPoolAddOperation);
        subsystemsManagement.getServerConfiguration().executeManagementOperation(compositeOperationBuilder.build().getOperation());
    }
}
