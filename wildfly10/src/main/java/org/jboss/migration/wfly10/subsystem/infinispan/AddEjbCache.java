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
package org.jboss.migration.wfly10.subsystem.infinispan;

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
 * A task which adds the 'ejb' cache, present on EAP 7 default configs, to Infinispan subsystem configuration.
 * @author emmartins
 */
public class AddEjbCache implements WildFly10SubsystemMigrationTask {

    public static final AddEjbCache INSTANCE = new AddEjbCache();

    private AddEjbCache() {
    }

    private static final String CACHE_CONTAINER = "cache-container";
    private static final String CACHE_NAME = "ejb";
    private static final String ALIASES_ATTR_NAME = "aliases";
    private static final String[] ALIASES_ATTR_VALUE = {"sfsb"};
    private static final String DEFAULT_CACHE_ATTR_NAME = "default-cache";
    private static final String DEFAULT_CACHE_ATTR_VALUE = "passivation";
    private static final String MODULE_ATTR_NAME = "module";
    private static final String MODULE_ATTR_VALUE = "org.wildfly.clustering.ejb.infinispan";
    private static final String LOCAL_CACHE = "local-cache";
    private static final String LOCAL_CACHE_NAME_PASSIVATION = "passivation";
    private static final String LOCAL_CACHE_NAME_PERSISTENCE = "persistent";

    private static final String COMPONENT = "component";
    private static final String COMPONENT_NAME_TRANSACTION = "transaction";
    private static final String MODE_ATTR_NAME = "mode";
    private static final String MODE_ATTR_VALUE_BATCH = "BATCH";

    private static final String COMPONENT_NAME_LOCKING = "locking";
    private static final String ISOLATION_ATTR_NAME = "isolation";
    private static final String ISOLATION_ATTR_VALUE_REPEATABLE_READ = "REPEATABLE_READ";

    private static final String STORE = "store";
    private static final String STORE_NAME_FILE = "file";
    private static final String PASSIVATION_ATTR_NAME = "passivation";
    private static final String PURGE_ATTR_NAME = "purge";

    @Override
    public void execute(ModelNode config, WildFly10Subsystem subsystem, WildFly10StandaloneServer server, ServerMigrationContext context) throws IOException {
        if (config == null) {
            return;
        }
        if (!config.hasDefined(CACHE_CONTAINER)) {
            ServerMigrationLogger.ROOT_LOGGER.infof("No Cache container");
            return;
        }
        if (config.hasDefined(CACHE_CONTAINER, CACHE_NAME)) {
            return;
        }
        /*
        <cache-container name="ejb" aliases="sfsb" default-cache="passivation" module="org.wildfly.clustering.ejb.infinispan">
                <local-cache name="passivation">
                    <locking isolation="REPEATABLE_READ"/>
                    <transaction mode="BATCH"/>
                    <file-store passivation="true" purge="false"/>
                </local-cache>
                <local-cache name="persistent">
                    <locking isolation="REPEATABLE_READ"/>
                    <transaction mode="BATCH"/>
                    <file-store passivation="false" purge="false"/>
                </local-cache>
            </cache-container>
         */
        final Operations.CompositeOperationBuilder compositeOperationBuilder = Operations.CompositeOperationBuilder.create();
        final PathAddress cachePathAddress = pathAddress(pathElement(SUBSYSTEM, subsystem.getName()), pathElement(CACHE_CONTAINER, CACHE_NAME));
        final ModelNode cacheAddOperation = Util.createAddOperation(cachePathAddress);
        for (String alias : ALIASES_ATTR_VALUE) {
            cacheAddOperation.get(ALIASES_ATTR_NAME).add(alias);
        }
        cacheAddOperation.get(DEFAULT_CACHE_ATTR_NAME).set(DEFAULT_CACHE_ATTR_VALUE);
        cacheAddOperation.get(MODULE_ATTR_NAME).set(MODULE_ATTR_VALUE);
        compositeOperationBuilder.addStep(cacheAddOperation);
        addLocalCachePassivation(compositeOperationBuilder, cachePathAddress);
        addLocalCachePersistent(compositeOperationBuilder, cachePathAddress);
        server.executeManagementOperation(compositeOperationBuilder.build().getOperation());
        ServerMigrationLogger.ROOT_LOGGER.infof("Ejb cache added to Infinispan subsystem configuration.");
    }

    private static void addLocalCachePassivation(Operations.CompositeOperationBuilder compositeOperationBuilder, PathAddress cachePathAddress) {
        // add local cache
        final PathAddress localCachePathAddress = addLocalCache(compositeOperationBuilder, cachePathAddress, LOCAL_CACHE_NAME_PASSIVATION);
        // set locking with isolation as REPEATABLE_READ
        setLocalCacheLocking(compositeOperationBuilder, localCachePathAddress, ISOLATION_ATTR_VALUE_REPEATABLE_READ);
        // set transaction with mode as BATCH
        setLocalCacheTransaction(compositeOperationBuilder, localCachePathAddress, MODE_ATTR_VALUE_BATCH);
        // set file store's passivation as true, and purge as false
        setLocalCacheFileStore(compositeOperationBuilder, localCachePathAddress, true, false);
    }

    private static void addLocalCachePersistent(Operations.CompositeOperationBuilder compositeOperationBuilder, PathAddress cachePathAddress) {
        // add local cache
        final PathAddress localCachePathAddress = addLocalCache(compositeOperationBuilder, cachePathAddress, LOCAL_CACHE_NAME_PERSISTENCE);
        // set locking with isolation as REPEATABLE_READ
        setLocalCacheLocking(compositeOperationBuilder, localCachePathAddress, ISOLATION_ATTR_VALUE_REPEATABLE_READ);
        // set transaction with mode as BATCH
        setLocalCacheTransaction(compositeOperationBuilder, localCachePathAddress, MODE_ATTR_VALUE_BATCH);
        // set file store's passivation as false, and purge as true
        setLocalCacheFileStore(compositeOperationBuilder, localCachePathAddress, false, true);
    }

    private static PathAddress addLocalCache(Operations.CompositeOperationBuilder compositeOperationBuilder, PathAddress cachePathAddress, String localCacheName) {
        // add local cache
        final PathAddress localCachePathAddress = cachePathAddress.append(LOCAL_CACHE, localCacheName);
        final ModelNode localCacheAddOperation = Util.createAddOperation(localCachePathAddress);
        compositeOperationBuilder.addStep(localCacheAddOperation);
        return localCachePathAddress;
    }

    private static void setLocalCacheLocking(Operations.CompositeOperationBuilder compositeOperationBuilder, PathAddress localCachePathAddress, String isolationValue) {
        final PathAddress localCacheIsolationPathAddress = localCachePathAddress.append(COMPONENT, COMPONENT_NAME_LOCKING);
        final ModelNode localCacheIsolationAddOperation = Util.createAddOperation(localCacheIsolationPathAddress);
        localCacheIsolationAddOperation.get(ISOLATION_ATTR_NAME).set(isolationValue);
        compositeOperationBuilder.addStep(localCacheIsolationAddOperation);
    }

    private static void setLocalCacheTransaction(Operations.CompositeOperationBuilder compositeOperationBuilder, PathAddress localCachePathAddress, String modeValue) {
        final PathAddress localCacheTransactionPathAddress = localCachePathAddress.append(COMPONENT, COMPONENT_NAME_TRANSACTION);
        final ModelNode localCacheTransactionAddOperation = Util.createAddOperation(localCacheTransactionPathAddress);
        localCacheTransactionAddOperation.get(MODE_ATTR_NAME).set(modeValue);
        compositeOperationBuilder.addStep(localCacheTransactionAddOperation);
    }

    private static void setLocalCacheFileStore(Operations.CompositeOperationBuilder compositeOperationBuilder, PathAddress localCachePathAddress, boolean passivation, boolean purge) {
        final PathAddress localCacheFileStorePathAddress = localCachePathAddress.append(STORE, STORE_NAME_FILE);
        final ModelNode localCacheFileStoreAddOperation = Util.createAddOperation(localCacheFileStorePathAddress);
        localCacheFileStoreAddOperation.get(PASSIVATION_ATTR_NAME).set(passivation);
        localCacheFileStoreAddOperation.get(PURGE_ATTR_NAME).set(purge);
        compositeOperationBuilder.addStep(localCacheFileStoreAddOperation);
    }
}
