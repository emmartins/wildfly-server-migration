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

package org.jboss.migration.wfly10.config.task.subsystem.infinispan;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.wfly10.config.management.SubsystemConfiguration;
import org.jboss.migration.wfly10.config.task.management.subsystem.UpdateSubsystemConfigurationSubtaskBuilder;

/**
 * A task which updates Infinispan subsystem configurations' 'web' cache, to match EAP 7.1 defaults.
 * @author emmartins
 */
public class UpdateWebCache<S> extends UpdateSubsystemConfigurationSubtaskBuilder<S> {

    public static final String TASK_NAME = "update-infinispan-web-cache";

    public UpdateWebCache() {
        super(TASK_NAME);
    }

    private static final String CACHE_CONTAINER = "cache-container";
    private static final String CACHE_CONTAINER_NAME = "web";

    private static final String DISTRIBUTED_CACHE = "distributed-cache";
    private static final String LOCAL_CACHE = "local-cache";
    private static final String CACHE_NAME = "concurrent";
    private static final String L1_LIFESPAN = "l1-lifespan";
    private static final String MODE = "mode";
    private static final String OWNERS = "owners";

    private static final String STORE = "store";
    private static final String STORE_NAME = "file";
    private static final String PASSIVATION = "passivation";
    private static final String PURGE = "purge";

    @Override
    protected ServerMigrationTaskResult updateConfiguration(ModelNode config, S source, SubsystemConfiguration subsystemConfiguration, TaskContext context, TaskEnvironment taskEnvironment) {
        if (!config.hasDefined(CACHE_CONTAINER)) {
            context.getLogger().debugf("No cache containers found, skipping task...");
            return ServerMigrationTaskResult.SKIPPED;
        }
        if (!config.hasDefined(CACHE_CONTAINER, CACHE_CONTAINER_NAME)) {
            context.getLogger().debugf("No cache container named %s found, skipping task...", CACHE_CONTAINER_NAME);
            return ServerMigrationTaskResult.SKIPPED;
        }
        final ModelNode cacheContainerConfig = config.get(CACHE_CONTAINER, CACHE_CONTAINER_NAME);
        final PathAddress cacheContainerPathAddress = subsystemConfiguration.getResourcePathAddress().append(PathElement.pathElement(CACHE_CONTAINER, CACHE_CONTAINER_NAME));
        final Operations.CompositeOperationBuilder compositeOperationBuilder = Operations.CompositeOperationBuilder.create();

        if (cacheContainerConfig.hasDefined(DISTRIBUTED_CACHE)) {
            // ha config
            if (cacheContainerConfig.hasDefined(DISTRIBUTED_CACHE, CACHE_NAME)) {
                context.getLogger().debugf("Cache container named %s already defines distributed cache named %s, skipping task...", CACHE_CONTAINER_NAME, CACHE_NAME);
                return ServerMigrationTaskResult.SKIPPED;
            }
                    /*
                    <distributed-cache name="concurrent" mode="SYNC" l1-lifespan="0" owners="2">
                        <file-store/>
                    </distributed-cache>
                    "concurrent" => {
                            "l1-lifespan" => 0L,
                            "mode" => "SYNC",
                            "owners" => 2,
                            "store" => {
                                "file" => {
                                }
                            }
                        }
                     */
            final PathAddress cachePathAddress = cacheContainerPathAddress.append(PathElement.pathElement(DISTRIBUTED_CACHE, CACHE_NAME));
            final ModelNode cacheAddOperation = Util.createAddOperation(cachePathAddress);
            cacheAddOperation.get(L1_LIFESPAN).set(0);
            cacheAddOperation.get(MODE).set("SYNC");
            cacheAddOperation.get(OWNERS).set(2);
            compositeOperationBuilder.addStep(cacheAddOperation);
            final ModelNode cacheFileStoreAddOperation = Util.createAddOperation(cachePathAddress.append(STORE, STORE_NAME));
            compositeOperationBuilder.addStep(cacheFileStoreAddOperation);
        } else {
            // local config
            if (cacheContainerConfig.hasDefined(LOCAL_CACHE, CACHE_NAME)) {
                context.getLogger().debugf("Cache container named %s already defines local cache named %s, skipping task...", CACHE_CONTAINER_NAME, CACHE_NAME);
                return ServerMigrationTaskResult.SKIPPED;
            }
                    /*
                        <local-cache name="concurrent">
                            <file-store passivation="true" purge="false"/>
                        </local-cache>

                        "concurrent" => {
                            "store" => {
                                "file" => {
                                    "passivation" => true,
                                    "purge" => false,
                                }
                            }
                        }
                     */
            final PathAddress cachePathAddress = cacheContainerPathAddress.append(PathElement.pathElement(LOCAL_CACHE, CACHE_NAME));
            final ModelNode cacheAddOperation = Util.createAddOperation(cachePathAddress);
            compositeOperationBuilder.addStep(cacheAddOperation);
            final ModelNode cacheFileStoreAddOperation = Util.createAddOperation(cachePathAddress.append(STORE, STORE_NAME));
            cacheFileStoreAddOperation.get(PASSIVATION).set(true);
            cacheFileStoreAddOperation.get(PURGE).set(false);
            compositeOperationBuilder.addStep(cacheFileStoreAddOperation);
        }
        subsystemConfiguration.getServerConfiguration().executeManagementOperation(compositeOperationBuilder.build().getOperation());
        context.getLogger().infof("Cache '%s' added to cache container '%s'.", CACHE_NAME, CACHE_CONTAINER_NAME);
        return ServerMigrationTaskResult.SUCCESS;
    }
}