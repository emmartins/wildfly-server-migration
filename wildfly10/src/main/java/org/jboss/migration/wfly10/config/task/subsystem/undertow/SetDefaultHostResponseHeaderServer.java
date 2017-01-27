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

package org.jboss.migration.wfly10.config.task.subsystem.undertow;

import org.jboss.dmr.ModelNode;
import org.jboss.migration.core.ProductInfo;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.core.env.TaskEnvironment;
import org.jboss.migration.wfly10.config.management.SubsystemConfiguration;

/**
 * @author emmartins
 */
public class SetDefaultHostResponseHeaderServer<S> extends SetDefaultHostResponseHeader<S> {
    public SetDefaultHostResponseHeaderServer() {
        super("server-header", "Server");
    }
    public SetDefaultHostResponseHeaderServer(String headerValue) {
        super("server-header", "Server", headerValue);
    }

    @Override
    protected String getHeaderValue(ModelNode config, SubsystemConfiguration subsystemConfiguration, TaskContext context, TaskEnvironment taskEnvironment) {
        if (headerValue != null) {
            return headerValue;
        } else {
            // compute from product info
            final ProductInfo productInfo = subsystemConfiguration.getServerConfiguration().getServer().getProductInfo();
            // TODO this should come instead, from the "subsystem" in extensions, exposed by target server
            final String serverName;
            if (productInfo.getName().contains("WildFly")) {
                serverName = "WildFly";
            } else if (productInfo.getName().contains("EAP")) {
                serverName = "JBoss-EAP";
            } else {
                serverName = productInfo.getName();
            }
            final int dot = productInfo.getVersion().indexOf('.');
            final String serverVersion;
            if (dot < 1) {
                serverVersion = productInfo.getVersion();
            } else {
                serverVersion = productInfo.getVersion().substring(0, dot);
            }
            return new StringBuilder(serverName).append('/').append(serverVersion).toString();
        }
    }
}
