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

package org.jboss.migration.wfly10.config.management;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.jboss.migration.wfly10.WildFlyServer10;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * @author emmartins
 */
public interface ManageableServerConfiguration extends ManageableResource, ExtensionConfiguration.Parent, InterfaceResource.Parent, SocketBindingGroupResource.Parent, SystemPropertyConfiguration.Parent {

    void start();
    void stop();
    boolean isStarted();
    ModelNode executeManagementOperation(ModelNode operation) throws IOException, ManagementOperationException;
    WildFlyServer10 getServer();
    Path resolvePath(String path)  throws IOException, ManagementOperationException;
    ModelControllerClient getModelControllerClient();

    class Type<T extends ManageableServerConfiguration> extends ManageableResource.Type<T> {

        static final ManageableResource.Type<?>[] CHILD_TYPES =  { ExtensionConfiguration.RESOURCE_TYPE, InterfaceResource.RESOURCE_TYPE, SocketBindingGroupResource.RESOURCE_TYPE, SystemPropertyConfiguration.RESOURCE_TYPE };

        protected Type(Class<T> type, ManageableResource.Type<?>... childTypes) {
            super(type, Stream.concat(Stream.of(CHILD_TYPES), Stream.of(childTypes)).toArray(ManageableResource.Type<?>[]::new));
        }
    }
}