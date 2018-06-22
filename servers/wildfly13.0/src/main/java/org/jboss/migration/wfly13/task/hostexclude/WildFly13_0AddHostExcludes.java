/*
 * Copyright 2018 Red Hat, Inc.
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
package org.jboss.migration.wfly13.task.hostexclude;

import org.jboss.migration.core.jboss.HostExclude;
import org.jboss.migration.core.jboss.HostExcludes;
import org.jboss.migration.wfly10.config.task.hostexclude.AddHostExcludes;

/**
 * @author emmartins
 */
public class WildFly13_0AddHostExcludes<S> extends AddHostExcludes<S> {

    private static final HostExcludes HOST_EXCLUDES = HostExcludes.builder()
            .hostExclude(HostExclude.builder()
                    .name("WildFly10.0")
                    .release("WildFly10.0")
                    .excludedExtension("org.wildfly.extension.core-management")
                    .excludedExtension("org.wildfly.extension.discovery")
                    .excludedExtension("org.wildfly.extension.ee-security")
                    .excludedExtension("org.wildfly.extension.elytron"))
            .hostExclude(HostExclude.builder()
                    .name("WildFly10.1")
                    .release("WildFly10.1")
                    .excludedExtension("org.wildfly.extension.core-management")
                    .excludedExtension("org.wildfly.extension.discovery")
                    .excludedExtension("org.wildfly.extension.ee-security")
                    .excludedExtension("org.wildfly.extension.elytron"))
            .hostExclude(HostExclude.builder()
                    .name("WildFly11.0")
                    .release("WildFly11.0")
                    .excludedExtension("org.wildfly.extension.ee-security"))
            .hostExclude(HostExclude.builder()
                    .name("WildFly12.0")
                    .release("WildFly12.0")
                    .excludedExtension("org.wildfly.extension.ee-security"))
            .build();

    public WildFly13_0AddHostExcludes() {
        super(HOST_EXCLUDES);
    }
}
