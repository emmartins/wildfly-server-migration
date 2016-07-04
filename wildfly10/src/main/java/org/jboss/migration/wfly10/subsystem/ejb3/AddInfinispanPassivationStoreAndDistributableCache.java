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
package org.jboss.migration.wfly10.subsystem.ejb3;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ServerMigrationTask;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskName;
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.wfly10.standalone.WildFly10StandaloneServer;
import org.jboss.migration.wfly10.subsystem.WildFly10Subsystem;
import org.jboss.migration.wfly10.subsystem.WildFly10SubsystemMigrationTask;
import org.jboss.migration.wfly10.subsystem.WildFly10SubsystemMigrationTaskFactory;

import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * A task which adds EJB3 subsystem's infinipan passivation-store, and distributable cache, if missing.
 * @author emmartins
 */
public class AddInfinispanPassivationStoreAndDistributableCache implements WildFly10SubsystemMigrationTaskFactory {

    public static final AddInfinispanPassivationStoreAndDistributableCache INSTANCE = new AddInfinispanPassivationStoreAndDistributableCache();

    public static final ServerMigrationTaskName SERVER_MIGRATION_TASK_NAME = new ServerMigrationTaskName.Builder().setName("add-infinispan-passivation-store-and-distributable-cache").build();

    private AddInfinispanPassivationStoreAndDistributableCache() {
    }

    private static final String CLUSTER_PASSIVATION_STORE = "cluster-passivation-store";
    private static final String FILE_PASSIVATION_STORE = "file-passivation-store";
    private static final String FILE_PASSIVATION_STORE_NAME = "file";
    private static final String PASSIVATION_STORE = "passivation-store";
    private static final String PASSIVATION_STORE_NAME = "infinispan";
    private static final String CACHE_CONTAINER_ATTR_NAME = "cache-container";
    private static final String CACHE_CONTAINER_ATTR_VALUE = "ejb";
    private static final String MAX_SIZE_ATTR_NAME = "max-size";
    private static final String MAX_SIZE_ATTR_VALUE = "10000";

    private static final String CACHE = "cache";
    private static final String CACHE_NAME_DISTRIBUTABLE = "distributable";
    private static final String CACHE_NAME_PASSIVATING = "passivating";
    private static final String CACHE_NAME_CLUSTERED = "clustered";
    private static final String ALIASES_ATTR_NAME = "aliases";
    private static final String[] ALIASES_ATTR_VALUE = {CACHE_NAME_PASSIVATING, CACHE_NAME_CLUSTERED};

    public static final String TASK_RESULT_ATTR_LEGACY_FILE_PASSIVATION_STORE_REMOVED = "legacy-file-passivation-store-removed";
    public static final String TASK_RESULT_ATTR_LEGACY_CLUSTERED_PASSIVATION_STORE_REMOVED = "legacy-clustered-passivation-store-removed";
    public static final String TASK_RESULT_ATTR_INFINISPAN_PASSIVATION_STORE_ADDED = "infinispan-passivation-store-added";
    public static final String TASK_RESULT_ATTR_LEGACY_PASSIVATING_CACHE_REMOVED = "legacy-passivating-cache-removed";
    public static final String TASK_RESULT_ATTR_LEGACY_CLUSTERED_CACHE_REMOVED = "legacy-clustered-cache-removed";
    public static final String TASK_RESULT_ATTR_DISTRIBUTABLE_CACHE_ADDED = "distributable-cache-added";

    @Override
    public ServerMigrationTask getServerMigrationTask(ModelNode config, WildFly10Subsystem subsystem, WildFly10StandaloneServer server) {
        return new WildFly10SubsystemMigrationTask(config, subsystem, server) {
            @Override
            public ServerMigrationTaskName getName() {
                return SERVER_MIGRATION_TASK_NAME;
            }
            @Override
            protected ServerMigrationTaskResult run(ModelNode config, WildFly10Subsystem subsystem, WildFly10StandaloneServer server, final ServerMigrationTaskContext context) throws Exception {
                if (config == null) {
                    return ServerMigrationTaskResult.SKIPPED;
                }

                final ServerMigrationTaskResult.Builder taskResultBuilder = new ServerMigrationTaskResult.Builder();
                boolean legacyFilePassivationStoreRemoved = false;
                boolean legacyClusteredPassivationStoreRemoved = false;
                boolean infinispanPassivationStoreAdded = false;
                boolean legacyPassivatingCacheRemoved = false;
                boolean legacyClusteredCacheRemoved = false;
                boolean distributableCacheAdded = false;

                boolean configUpdated = false;
                if (!config.hasDefined(PASSIVATION_STORE, PASSIVATION_STORE_NAME)) {
                    // replace all passivation stores with WFLY 10 default one
                    // remove file-passivation-store
                    if (config.hasDefined(FILE_PASSIVATION_STORE, FILE_PASSIVATION_STORE_NAME)) {
                        final PathAddress filePassivationStorePathAddress = pathAddress(pathElement(SUBSYSTEM, subsystem.getName()), pathElement(FILE_PASSIVATION_STORE, FILE_PASSIVATION_STORE_NAME));
                        final ModelNode filePassivationStoreRemoveperation = Util.createRemoveOperation(filePassivationStorePathAddress);
                        server.executeManagementOperation(filePassivationStoreRemoveperation);
                        context.getLogger().infof("Legacy file passivation store removed from EJB3 subsystem configuration.");
                        legacyFilePassivationStoreRemoved = true;
                    }
                    // remove cluster-passivation-store infinispan
                    if (config.hasDefined(CLUSTER_PASSIVATION_STORE, PASSIVATION_STORE_NAME)) {
                        final PathAddress filePassivationStorePathAddress = pathAddress(pathElement(SUBSYSTEM, subsystem.getName()), pathElement(CLUSTER_PASSIVATION_STORE, PASSIVATION_STORE_NAME));
                        final ModelNode filePassivationStoreRemoveperation = Util.createRemoveOperation(filePassivationStorePathAddress);
                        server.executeManagementOperation(filePassivationStoreRemoveperation);
                        context.getLogger().infof("Legacy 'clustered' passivation store removed from EJB3 subsystem configuration.");
                        legacyClusteredPassivationStoreRemoved = true;
                    }
                    // add default wfly 10 / eap 7 infinispan passivation store
                    final PathAddress passivationStorePathAddress = pathAddress(pathElement(SUBSYSTEM, subsystem.getName()), pathElement(PASSIVATION_STORE, PASSIVATION_STORE_NAME));
                    final ModelNode passivationStoreAddOperation = Util.createAddOperation(passivationStorePathAddress);
                    passivationStoreAddOperation.get(CACHE_CONTAINER_ATTR_NAME).set(CACHE_CONTAINER_ATTR_VALUE);
                    passivationStoreAddOperation.get(MAX_SIZE_ATTR_NAME).set(MAX_SIZE_ATTR_VALUE);
                    server.executeManagementOperation(passivationStoreAddOperation);
                    configUpdated = true;
                    context.getLogger().infof("Infinispan passivation store added to EJB3 subsystem configuration.");
                    infinispanPassivationStoreAdded = true;
                }
                if (!config.hasDefined(CACHE, CACHE_NAME_DISTRIBUTABLE)) {
                    // remove legacy passivating cache
                    if (config.hasDefined(CACHE, CACHE_NAME_PASSIVATING)) {
                        final PathAddress cachePathAddress = pathAddress(pathElement(SUBSYSTEM, subsystem.getName()), pathElement(CACHE, CACHE_NAME_PASSIVATING));
                        final ModelNode cacheRemoveOperation = Util.createRemoveOperation(cachePathAddress);
                        server.executeManagementOperation(cacheRemoveOperation);
                        context.getLogger().infof("Legacy 'passivating' cache removed from EJB3 subsystem configuration.");
                        legacyPassivatingCacheRemoved = true;
                    }
                    // remove legacy clustered cache
                    if (config.hasDefined(CACHE, CACHE_NAME_CLUSTERED)) {
                        final PathAddress cachePathAddress = pathAddress(pathElement(SUBSYSTEM, subsystem.getName()), pathElement(CACHE, CACHE_NAME_CLUSTERED));
                        final ModelNode cacheRemoveOperation = Util.createRemoveOperation(cachePathAddress);
                        server.executeManagementOperation(cacheRemoveOperation);
                        context.getLogger().infof("Legacy 'clustered' cache removed from EJB3 subsystem configuration.");
                        legacyClusteredCacheRemoved = true;
                    }
                    // add wfly 10 / eap 7 default distributable cache
                    final PathAddress cachePathAddress = pathAddress(pathElement(SUBSYSTEM, subsystem.getName()), pathElement(CACHE, CACHE_NAME_DISTRIBUTABLE));
                    final ModelNode cacheAddOperation = Util.createAddOperation(cachePathAddress);
                    cacheAddOperation.get(PASSIVATION_STORE).set(PASSIVATION_STORE_NAME);
                    for (String alias : ALIASES_ATTR_VALUE) {
                        cacheAddOperation.get(ALIASES_ATTR_NAME).add(alias);
                    }
                    server.executeManagementOperation(cacheAddOperation);
                    configUpdated = true;
                    context.getLogger().infof("Distributable cache added to EJB3 subsystem configuration.");
                    distributableCacheAdded = true;
                }
                if (configUpdated) {
                    taskResultBuilder.sucess();
                } else {
                    taskResultBuilder.skipped();
                }
                return taskResultBuilder.addAttribute(TASK_RESULT_ATTR_LEGACY_FILE_PASSIVATION_STORE_REMOVED, legacyFilePassivationStoreRemoved)
                        .addAttribute(TASK_RESULT_ATTR_LEGACY_CLUSTERED_PASSIVATION_STORE_REMOVED, legacyClusteredPassivationStoreRemoved)
                        .addAttribute(TASK_RESULT_ATTR_INFINISPAN_PASSIVATION_STORE_ADDED, infinispanPassivationStoreAdded)
                        .addAttribute(TASK_RESULT_ATTR_LEGACY_PASSIVATING_CACHE_REMOVED, legacyPassivatingCacheRemoved)
                        .addAttribute(TASK_RESULT_ATTR_LEGACY_CLUSTERED_CACHE_REMOVED, legacyClusteredCacheRemoved)
                        .addAttribute(TASK_RESULT_ATTR_DISTRIBUTABLE_CACHE_ADDED, distributableCacheAdded).build();

            }
        };
    }
}
