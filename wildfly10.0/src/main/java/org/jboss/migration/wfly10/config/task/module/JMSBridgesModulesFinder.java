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

package org.jboss.migration.wfly10.config.task.module;

import org.jboss.migration.core.jboss.ModulesMigrationTask;
import org.jboss.migration.core.task.TaskContext;
import org.jboss.migration.wfly10.config.task.subsystem.SubsystemNames;

import javax.xml.stream.XMLStreamReader;
import java.io.IOException;

/**
 * Finds modules referenced by Messaging subsystem configs, as source of JMS Bridges.
 * @author emmartins
 */
public class JMSBridgesModulesFinder implements ConfigurationModulesMigrationTaskFactory.ModulesFinder {
    @Override
    public String getElementLocalName() {
        return "jms-bridge";
    }

    @Override
    public void processElement(XMLStreamReader reader, ModulesMigrationTask.ModuleMigrator moduleMigrator, TaskContext context) throws IOException {
        final String namespaceURI = reader.getNamespaceURI();
        if (namespaceURI == null || !namespaceURI.startsWith("urn:jboss:domain:"+ SubsystemNames.MESSAGING_ACTIVEMQ)) {
            return;
        }
        final String moduleId = reader.getAttributeValue(null, "module");
        if (moduleId != null) {
            moduleMigrator.migrateModule(moduleId, "Required by JMS Bridge " + reader.getAttributeValue(null, "name"), context);
        }
    }

    /*
    <jms-bridge name="myBridge" module="org.acmemq">
      <source connection-factory="ConnectionFactory"
              destination="sourceQ"
              user="user1"
              password="pwd1"
              quality-of-service="AT_MOST_ONCE"
              failure-retry-interval="500"
              max-retries="1"
              max-batch-size="500"
              max-batch-time="500"
              add-messageID-in-header="true">
         <source-context>
            <property name="java.naming.factory.initial"
                      value="org.acmemq.jndi.AcmeMQInitialContextFactory"/>
            <property name="java.naming.provider.url"
                      value="tcp://127.0.0.1:9292"/>
         </source-context>
      </source>
      <target connection-factory"/jms/invmTargetCF"
              destination="/jms/targetQ" />
      </target>
   </jms-bridge>
     */
}
