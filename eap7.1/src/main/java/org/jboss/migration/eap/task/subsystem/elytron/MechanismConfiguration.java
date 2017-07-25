/*
 * Copyright 2017 Red Hat, Inc.
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

package org.jboss.migration.eap.task.subsystem.elytron;

import org.jboss.dmr.ModelNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emmartins
 */
public class MechanismConfiguration {
    private final String mechanismName;
    private String realmMapper;
    private List<MechanismRealmConfiguration> mechanismRealmConfigurations;

    public MechanismConfiguration(String mechanismName) {
        this.mechanismName = mechanismName;
        this.mechanismRealmConfigurations = new ArrayList<>();
    }

    public MechanismConfiguration realmMapper(String realmMapper) {
        this.realmMapper = realmMapper;
        return this;
    }

    public MechanismConfiguration addMechanismRealmConfiguration(MechanismRealmConfiguration mechanismRealmConfiguration) {
        this.mechanismRealmConfigurations.add(mechanismRealmConfiguration);
        return this;
    }

    public ModelNode toModelNode() {
        final ModelNode modelNode = new ModelNode();
        modelNode.get("mechanism-name").set(mechanismName);
        if (realmMapper != null) {
            modelNode.get("realm-mapper").set(realmMapper);
        }
        if (mechanismRealmConfigurations != null && !mechanismRealmConfigurations.isEmpty()) {
            final ModelNode mechanismRealmConfigurationsNode = modelNode.get("mechanism-realm-configurations").setEmptyList();
            for (MechanismRealmConfiguration mechanismRealmConfiguration : mechanismRealmConfigurations) {
                mechanismRealmConfigurationsNode.add(mechanismRealmConfiguration.toModelNode());
            }
        }
        return modelNode;
    }
}
