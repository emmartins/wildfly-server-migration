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
package org.jboss.migration.eap.task.hostexclude;

import org.jboss.migration.core.jboss.HostExclude;
import org.jboss.migration.core.jboss.HostExcludes;
import org.jboss.migration.wfly10.config.task.hostexclude.AddHostExcludes;

/**
 * @author emmartins
 */
public class EAP7_2AddHostExcludes<S> extends AddHostExcludes<S> {

    private static final HostExcludes HOST_EXCLUDES = HostExcludes.builder()
            /*
            <host-exclude name="EAP62">
                <host-release id="EAP6.2"/>
                <excluded-extensions>
                    <extension module="org.wildfly.extension.batch.jberet"/>
                    <extension module="org.wildfly.extension.bean-validation"/>
                    <extension module="org.wildfly.extension.clustering.singleton"/>
                    <extension module="org.wildfly.extension.core-management"/>
                    <extension module="org.wildfly.extension.datasources-agroal"/>
                    <extension module="org.wildfly.extension.discovery"/>
                    <extension module="org.wildfly.extension.ee-security"/>
                    <extension module="org.wildfly.extension.elytron"/>
                    <extension module="org.wildfly.extension.io"/>
                    <extension module="org.wildfly.extension.messaging-activemq"/>
                    <extension module="org.wildfly.extension.microprofile.config-smallrye"/>
                    <extension module="org.wildfly.extension.microprofile.health-smallrye"/>
                    <extension module="org.wildfly.extension.microprofile.opentracing-smallrye"/>
                    <extension module="org.wildfly.extension.request-controller"/>
                    <extension module="org.wildfly.extension.security.manager"/>
                    <extension module="org.wildfly.extension.undertow"/>
                    <extension module="org.wildfly.iiop-openjdk"/>
                </excluded-extensions>
            </host-exclude>
             */
            .hostExclude(HostExclude.builder()
                    .name("EAP62")
                    .release("EAP6.2")
                    .excludedExtension("org.wildfly.extension.batch.jberet")
                    .excludedExtension("org.wildfly.extension.bean-validation")
                    .excludedExtension("org.wildfly.extension.clustering.singleton")
                    .excludedExtension("org.wildfly.extension.core-management")
                    .excludedExtension("org.wildfly.extension.datasources-agroal")
                    .excludedExtension("org.wildfly.extension.discovery")
                    .excludedExtension("org.wildfly.extension.ee-security")
                    .excludedExtension("org.wildfly.extension.elytron")
                    .excludedExtension("org.wildfly.extension.io")
                    .excludedExtension("org.wildfly.extension.messaging-activemq")
                    .excludedExtension("org.wildfly.extension.microprofile.config-smallrye")
                    .excludedExtension("org.wildfly.extension.microprofile.health-smallrye")
                    .excludedExtension("org.wildfly.extension.microprofile.opentracing-smallrye")
                    .excludedExtension("org.wildfly.extension.request-controller")
                    .excludedExtension("org.wildfly.extension.security.manager")
                    .excludedExtension("org.wildfly.extension.undertow")
                    .excludedExtension("org.wildfly.iiop-openjdk"))
            /*
            <host-exclude name="EAP63">
                <host-release id="EAP6.3"/>
                <excluded-extensions>
                    <extension module="org.wildfly.extension.batch.jberet"/>
                    <extension module="org.wildfly.extension.bean-validation"/>
                    <extension module="org.wildfly.extension.clustering.singleton"/>
                    <extension module="org.wildfly.extension.core-management"/>
                    <extension module="org.wildfly.extension.datasources-agroal"/>
                    <extension module="org.wildfly.extension.discovery"/>
                    <extension module="org.wildfly.extension.ee-security"/>
                    <extension module="org.wildfly.extension.elytron"/>
                    <extension module="org.wildfly.extension.io"/>
                    <extension module="org.wildfly.extension.messaging-activemq"/>
                    <extension module="org.wildfly.extension.microprofile.config-smallrye"/>
                    <extension module="org.wildfly.extension.microprofile.health-smallrye"/>
                    <extension module="org.wildfly.extension.microprofile.opentracing-smallrye"/>
                    <extension module="org.wildfly.extension.request-controller"/>
                    <extension module="org.wildfly.extension.security.manager"/>
                    <extension module="org.wildfly.extension.undertow"/>
                    <extension module="org.wildfly.iiop-openjdk"/>
                </excluded-extensions>
            </host-exclude>
             */

            .hostExclude(HostExclude.builder()
                    .name("EAP63")
                    .release("EAP6.3")
                    .excludedExtension("org.wildfly.extension.batch.jberet")
                    .excludedExtension("org.wildfly.extension.bean-validation")
                    .excludedExtension("org.wildfly.extension.clustering.singleton")
                    .excludedExtension("org.wildfly.extension.core-management")
                    .excludedExtension("org.wildfly.extension.datasources-agroal")
                    .excludedExtension("org.wildfly.extension.discovery")
                    .excludedExtension("org.wildfly.extension.ee-security")
                    .excludedExtension("org.wildfly.extension.elytron")
                    .excludedExtension("org.wildfly.extension.io")
                    .excludedExtension("org.wildfly.extension.messaging-activemq")
                    .excludedExtension("org.wildfly.extension.microprofile.config-smallrye")
                    .excludedExtension("org.wildfly.extension.microprofile.health-smallrye")
                    .excludedExtension("org.wildfly.extension.microprofile.opentracing-smallrye")
                    .excludedExtension("org.wildfly.extension.request-controller")
                    .excludedExtension("org.wildfly.extension.security.manager")
                    .excludedExtension("org.wildfly.extension.undertow")
                    .excludedExtension("org.wildfly.iiop-openjdk"))
            /*
            <host-exclude name="EAP64">
                <host-release id="EAP6.4"/>
                <excluded-extensions>
                    <extension module="org.wildfly.extension.batch.jberet"/>
                    <extension module="org.wildfly.extension.bean-validation"/>
                    <extension module="org.wildfly.extension.clustering.singleton"/>
                    <extension module="org.wildfly.extension.core-management"/>
                    <extension module="org.wildfly.extension.datasources-agroal"/>
                    <extension module="org.wildfly.extension.discovery"/>
                    <extension module="org.wildfly.extension.ee-security"/>
                    <extension module="org.wildfly.extension.elytron"/>
                    <extension module="org.wildfly.extension.io"/>
                    <extension module="org.wildfly.extension.messaging-activemq"/>
                    <extension module="org.wildfly.extension.microprofile.config-smallrye"/>
                    <extension module="org.wildfly.extension.microprofile.health-smallrye"/>
                    <extension module="org.wildfly.extension.microprofile.opentracing-smallrye"/>
                    <extension module="org.wildfly.extension.request-controller"/>
                    <extension module="org.wildfly.extension.security.manager"/>
                    <extension module="org.wildfly.extension.undertow"/>
                    <extension module="org.wildfly.iiop-openjdk"/>
                </excluded-extensions>
            </host-exclude>
             */
            .hostExclude(HostExclude.builder()
                    .name("EAP64")
                    .release("EAP6.4")
                    .excludedExtension("org.wildfly.extension.batch.jberet")
                    .excludedExtension("org.wildfly.extension.bean-validation")
                    .excludedExtension("org.wildfly.extension.clustering.singleton")
                    .excludedExtension("org.wildfly.extension.core-management")
                    .excludedExtension("org.wildfly.extension.datasources-agroal")
                    .excludedExtension("org.wildfly.extension.discovery")
                    .excludedExtension("org.wildfly.extension.ee-security")
                    .excludedExtension("org.wildfly.extension.elytron")
                    .excludedExtension("org.wildfly.extension.io")
                    .excludedExtension("org.wildfly.extension.messaging-activemq")
                    .excludedExtension("org.wildfly.extension.microprofile.config-smallrye")
                    .excludedExtension("org.wildfly.extension.microprofile.health-smallrye")
                    .excludedExtension("org.wildfly.extension.microprofile.opentracing-smallrye")
                    .excludedExtension("org.wildfly.extension.request-controller")
                    .excludedExtension("org.wildfly.extension.security.manager")
                    .excludedExtension("org.wildfly.extension.undertow")
                    .excludedExtension("org.wildfly.iiop-openjdk"))
            /*
              <host-exclude name="EAP64z">
                <host-api-version major-version="1" minor-version="8"/>
                <excluded-extensions>
                    <extension module="org.wildfly.extension.batch.jberet"/>
                    <extension module="org.wildfly.extension.bean-validation"/>
                    <extension module="org.wildfly.extension.clustering.singleton"/>
                    <extension module="org.wildfly.extension.core-management"/>
                    <extension module="org.wildfly.extension.datasources-agroal"/>
                    <extension module="org.wildfly.extension.discovery"/>
                    <extension module="org.wildfly.extension.ee-security"/>
                    <extension module="org.wildfly.extension.elytron"/>
                    <extension module="org.wildfly.extension.io"/>
                    <extension module="org.wildfly.extension.messaging-activemq"/>
                    <extension module="org.wildfly.extension.microprofile.config-smallrye"/>
                    <extension module="org.wildfly.extension.microprofile.health-smallrye"/>
                    <extension module="org.wildfly.extension.microprofile.opentracing-smallrye"/>
                    <extension module="org.wildfly.extension.request-controller"/>
                    <extension module="org.wildfly.extension.security.manager"/>
                    <extension module="org.wildfly.extension.undertow"/>
                    <extension module="org.wildfly.iiop-openjdk"/>
                </excluded-extensions>
            </host-exclude>
             */
            .hostExclude(HostExclude.builder()
                    .name("EAP64z")
                    .apiVersion("1","8")
                    .excludedExtension("org.wildfly.extension.batch.jberet")
                    .excludedExtension("org.wildfly.extension.bean-validation")
                    .excludedExtension("org.wildfly.extension.clustering.singleton")
                    .excludedExtension("org.wildfly.extension.core-management")
                    .excludedExtension("org.wildfly.extension.datasources-agroal")
                    .excludedExtension("org.wildfly.extension.discovery")
                    .excludedExtension("org.wildfly.extension.ee-security")
                    .excludedExtension("org.wildfly.extension.elytron")
                    .excludedExtension("org.wildfly.extension.io")
                    .excludedExtension("org.wildfly.extension.messaging-activemq")
                    .excludedExtension("org.wildfly.extension.microprofile.config-smallrye")
                    .excludedExtension("org.wildfly.extension.microprofile.health-smallrye")
                    .excludedExtension("org.wildfly.extension.microprofile.opentracing-smallrye")
                    .excludedExtension("org.wildfly.extension.request-controller")
                    .excludedExtension("org.wildfly.extension.security.manager")
                    .excludedExtension("org.wildfly.extension.undertow")
                    .excludedExtension("org.wildfly.iiop-openjdk"))
            /*
            <host-exclude name="EAP70">
                <host-release id="EAP7.0"/>
                <excluded-extensions>
                    <extension module="org.wildfly.extension.core-management"/>
                    <extension module="org.wildfly.extension.datasources-agroal"/>
                    <extension module="org.wildfly.extension.discovery"/>
                    <extension module="org.wildfly.extension.ee-security"/>
                    <extension module="org.wildfly.extension.elytron"/>
                    <extension module="org.wildfly.extension.microprofile.config-smallrye"/>
                    <extension module="org.wildfly.extension.microprofile.health-smallrye"/>
                    <extension module="org.wildfly.extension.microprofile.opentracing-smallrye"/>
                </excluded-extensions>
            </host-exclude>
             */
            .hostExclude(HostExclude.builder()
                    .name("EAP70")
                    .release("EAP7.0")
                    .excludedExtension("org.wildfly.extension.core-management")
                    .excludedExtension("org.wildfly.extension.datasources-agroal")
                    .excludedExtension("org.wildfly.extension.discovery")
                    .excludedExtension("org.wildfly.extension.ee-security")
                    .excludedExtension("org.wildfly.extension.elytron")
                    .excludedExtension("org.wildfly.extension.microprofile.config-smallrye")
                    .excludedExtension("org.wildfly.extension.microprofile.health-smallrye")
                    .excludedExtension("org.wildfly.extension.microprofile.opentracing-smallrye"))
            /*
            <host-exclude name="EAP71">
                <host-release id="EAP7.1"/>
                <excluded-extensions>
                    <extension module="org.wildfly.extension.datasources-agroal"/>
                    <extension module="org.wildfly.extension.ee-security"/>
                    <extension module="org.wildfly.extension.microprofile.config-smallrye"/>
                    <extension module="org.wildfly.extension.microprofile.health-smallrye"/>
                    <extension module="org.wildfly.extension.microprofile.opentracing-smallrye"/>
                </excluded-extensions>
            </host-exclude>
             */
            .hostExclude(HostExclude.builder()
                    .name("EAP71")
                    .release("EAP7.1")
                    .excludedExtension("org.wildfly.extension.datasources-agroal")
                    .excludedExtension("org.wildfly.extension.ee-security")
                    .excludedExtension("org.wildfly.extension.microprofile.config-smallrye")
                    .excludedExtension("org.wildfly.extension.microprofile.health-smallrye")
                    .excludedExtension("org.wildfly.extension.microprofile.opentracing-smallrye"))
            .build();

    public EAP7_2AddHostExcludes() {
        super(HOST_EXCLUDES);
    }
}
