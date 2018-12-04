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
package org.jboss.migration.wfly.task.hostexclude;

import org.jboss.migration.core.jboss.HostExclude;
import org.jboss.migration.core.jboss.HostExcludes;
import org.jboss.migration.wfly10.config.task.hostexclude.AddHostExcludes;

/**
 * @author emmartins
 */
public class WildFly15_0AddHostExcludes<S> extends AddHostExcludes<S> {

    /*
    <host-excludes>
        <host-exclude name="WildFly10.0">
            <host-release id="WildFly10.0"/>
            <excluded-extensions>
                <extension module="org.wildfly.extension.core-management"/>
                <extension module="org.wildfly.extension.datasources-agroal"/>
                <extension module="org.wildfly.extension.discovery"/>
                <extension module="org.wildfly.extension.ee-security"/>
                <extension module="org.wildfly.extension.elytron"/>
                <extension module="org.wildfly.extension.microprofile.config-smallrye"/>
                <extension module="org.wildfly.extension.microprofile.health-smallrye"/>
                <extension module="org.wildfly.extension.microprofile.opentracing-smallrye"/>
                <extension module="org.wildfly.extension.microprofile.metrics-smallrye"/>
            </excluded-extensions>
        </host-exclude>
        <host-exclude name="WildFly10.1">
            <host-release id="WildFly10.1"/>
            <excluded-extensions>
                <extension module="org.wildfly.extension.core-management"/>
                <extension module="org.wildfly.extension.datasources-agroal"/>
                <extension module="org.wildfly.extension.discovery"/>
                <extension module="org.wildfly.extension.ee-security"/>
                <extension module="org.wildfly.extension.elytron"/>
                <extension module="org.wildfly.extension.microprofile.config-smallrye"/>
                <extension module="org.wildfly.extension.microprofile.health-smallrye"/>
                <extension module="org.wildfly.extension.microprofile.opentracing-smallrye"/>
                <extension module="org.wildfly.extension.microprofile.metrics-smallrye"/>
            </excluded-extensions>
        </host-exclude>
        <host-exclude name="WildFly11.0">
            <host-release id="WildFly11.0"/>
            <excluded-extensions>
                <extension module="org.wildfly.extension.datasources-agroal"/>
                <extension module="org.wildfly.extension.ee-security"/>
                <extension module="org.wildfly.extension.microprofile.config-smallrye"/>
                <extension module="org.wildfly.extension.microprofile.health-smallrye"/>
                <extension module="org.wildfly.extension.microprofile.opentracing-smallrye"/>
                <extension module="org.wildfly.extension.microprofile.metrics-smallrye"/>
            </excluded-extensions>
        </host-exclude>
        <host-exclude name="WildFly12.0">
            <host-release id="WildFly12.0"/>
            <excluded-extensions>
                <extension module="org.wildfly.extension.datasources-agroal"/>
                <extension module="org.wildfly.extension.ee-security"/>
                <extension module="org.wildfly.extension.microprofile.config-smallrye"/>
                <extension module="org.wildfly.extension.microprofile.health-smallrye"/>
                <extension module="org.wildfly.extension.microprofile.opentracing-smallrye"/>
                <extension module="org.wildfly.extension.microprofile.metrics-smallrye"/>
            </excluded-extensions>
        </host-exclude>
        <host-exclude name="WildFly13.0">
            <host-release id="WildFly13.0"/>
            <excluded-extensions>
                <extension module="org.wildfly.extension.datasources-agroal"/>
                <extension module="org.wildfly.extension.microprofile.config-smallrye"/>
                <extension module="org.wildfly.extension.microprofile.health-smallrye"/>
                <extension module="org.wildfly.extension.microprofile.opentracing-smallrye"/>
                <extension module="org.wildfly.extension.microprofile.metrics-smallrye"/>
            </excluded-extensions>
        </host-exclude>
        <host-exclude name="WildFly14.0">
            <host-release id="WildFly14.0"/>
            <excluded-extensions>
                <extension module="org.wildfly.extension.microprofile.metrics-smallrye"/>
            </excluded-extensions>
        </host-exclude>
    </host-excludes>
     */
    private static final HostExcludes HOST_EXCLUDES = HostExcludes.builder()
            .hostExclude(HostExclude.builder()
                    .name("WildFly10.0")
                    .release("WildFly10.0")
                    .excludedExtension("org.wildfly.extension.core-management")
                    .excludedExtension("org.wildfly.extension.datasources-agroal")
                    .excludedExtension("org.wildfly.extension.discovery")
                    .excludedExtension("org.wildfly.extension.ee-security")
                    .excludedExtension("org.wildfly.extension.elytron")
                    .excludedExtension("org.wildfly.extension.microprofile.config-smallrye")
                    .excludedExtension("org.wildfly.extension.microprofile.health-smallrye")
                    .excludedExtension("org.wildfly.extension.microprofile.opentracing-smallrye")
                    .excludedExtension("org.wildfly.extension.microprofile.metrics-smallrye"))
            .hostExclude(HostExclude.builder()
                    .name("WildFly10.1")
                    .release("WildFly10.1")
                    .excludedExtension("org.wildfly.extension.core-management")
                    .excludedExtension("org.wildfly.extension.datasources-agroal")
                    .excludedExtension("org.wildfly.extension.discovery")
                    .excludedExtension("org.wildfly.extension.ee-security")
                    .excludedExtension("org.wildfly.extension.elytron")
                    .excludedExtension("org.wildfly.extension.microprofile.config-smallrye")
                    .excludedExtension("org.wildfly.extension.microprofile.health-smallrye")
                    .excludedExtension("org.wildfly.extension.microprofile.opentracing-smallrye")
                    .excludedExtension("org.wildfly.extension.microprofile.metrics-smallrye"))
            .hostExclude(HostExclude.builder()
                    .name("WildFly11.0")
                    .release("WildFly11.0")
                    .excludedExtension("org.wildfly.extension.datasources-agroal")
                    .excludedExtension("org.wildfly.extension.ee-security")
                    .excludedExtension("org.wildfly.extension.microprofile.config-smallrye")
                    .excludedExtension("org.wildfly.extension.microprofile.health-smallrye")
                    .excludedExtension("org.wildfly.extension.microprofile.opentracing-smallrye")
                    .excludedExtension("org.wildfly.extension.microprofile.metrics-smallrye"))
            .hostExclude(HostExclude.builder()
                    .name("WildFly12.0")
                    .release("WildFly12.0")
                    .excludedExtension("org.wildfly.extension.datasources-agroal")
                    .excludedExtension("org.wildfly.extension.ee-security")
                    .excludedExtension("org.wildfly.extension.microprofile.config-smallrye")
                    .excludedExtension("org.wildfly.extension.microprofile.health-smallrye")
                    .excludedExtension("org.wildfly.extension.microprofile.opentracing-smallrye")
                    .excludedExtension("org.wildfly.extension.microprofile.metrics-smallrye"))
            .hostExclude(HostExclude.builder()
                    .name("WildFly13.0")
                    .release("WildFly13.0")
                    .excludedExtension("org.wildfly.extension.datasources-agroal")
                    .excludedExtension("org.wildfly.extension.microprofile.config-smallrye")
                    .excludedExtension("org.wildfly.extension.microprofile.health-smallrye")
                    .excludedExtension("org.wildfly.extension.microprofile.opentracing-smallrye")
                    .excludedExtension("org.wildfly.extension.microprofile.metrics-smallrye"))
            .hostExclude(HostExclude.builder()
                    .name("WildFly14.0")
                    .release("WildFly14.0")
                    .excludedExtension("org.wildfly.extension.microprofile.metrics-smallrye"))
            .build();

    public WildFly15_0AddHostExcludes() {
        super(HOST_EXCLUDES);
    }
}
