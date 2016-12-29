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
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.core.task.ServerMigrationTaskResult;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.wfly10.config.management.ManageableServerConfiguration;
import org.jboss.migration.wfly10.config.management.SubsystemResource;
import org.jboss.migration.wfly10.config.task.management.subsystem.UpdateSubsystemResourceSubtaskBuilder;

import java.util.Arrays;
import java.util.List;

import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * A task which changes the module name of any Hibernate Cache present in Infinispan subsystem.
 * @author emmartins
 */
public class FixHibernateCacheModuleName<S> extends UpdateSubsystemResourceSubtaskBuilder<S> {

    public interface EnvironmentProperties {
        String LEGACY_MODULE_NAMES = "deprecatedModuleNames";
        String NEW_MODULE_NAME = "moduleName";
    }

    public static final String TASK_NAME = "fix-hibernate-cache-module-name";

    public FixHibernateCacheModuleName() {
        super(TASK_NAME);
    }

    private static final String CACHE_CONTAINER = "cache-container";
    private static final String MODULE_ATTR_NAME = "module";
    private static final String DEFAULT_NEW_MODULE_NAME = "org.hibernate.infinispan";
    private static final List<String> DEFAULT_LEGACY_MODULE_NAMES = Arrays.asList("org.jboss.as.jpa.hibernate:4", "org.hibernate");

    @Override
    protected ServerMigrationTaskResult updateConfiguration(ModelNode config, S source, SubsystemResource subsystemResource, TaskContext context, TaskEnvironment taskEnvironment) {
        final PathAddress subsystemPathAddress = subsystemResource.getResourcePathAddress();
        final ManageableServerConfiguration configurationManagement = subsystemResource.getServerConfiguration();
        // read env properties
        final List<String> legacyModuleNames = taskEnvironment.getPropertyAsList(EnvironmentProperties.LEGACY_MODULE_NAMES, DEFAULT_LEGACY_MODULE_NAMES);
        final String newModuleName = taskEnvironment.getPropertyAsString(EnvironmentProperties.NEW_MODULE_NAME, DEFAULT_NEW_MODULE_NAME);
        // do migration
        if (!config.hasDefined(CACHE_CONTAINER)) {
            context.getLogger().infof("No Cache container");
            return ServerMigrationTaskResult.SKIPPED;
        }
        boolean configUpdated = false;
        for (String cacheName : config.get(CACHE_CONTAINER).keys()) {
            final ModelNode cache = config.get(CACHE_CONTAINER, cacheName);
            if (cache.hasDefined(MODULE_ATTR_NAME)) {
                if (legacyModuleNames.contains(cache.get(MODULE_ATTR_NAME).asString())) {
                    // /subsystem=infinispan/cache-container=cacheName:write-attribute(name=MODULE_ATTR_NAME,value=MODULE_ATTR_NEW_VALUE)
                    final ModelNode op = Util.createEmptyOperation(WRITE_ATTRIBUTE_OPERATION, subsystemPathAddress.append(pathElement(CACHE_CONTAINER, cacheName)));
                    op.get(NAME).set(MODULE_ATTR_NAME);
                    op.get(VALUE).set(newModuleName);
                    configurationManagement.executeManagementOperation(op);
                    configUpdated = true;
                    context.getLogger().infof("Infinispan subsystem's cache %s 'module' attribute updated to %s.", cacheName, newModuleName);
                }
            }
        }
        return configUpdated ? ServerMigrationTaskResult.SUCCESS : ServerMigrationTaskResult.SKIPPED;
    }
}