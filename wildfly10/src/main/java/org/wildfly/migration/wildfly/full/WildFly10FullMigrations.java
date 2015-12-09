/*
 * Copyright 2015 Red Hat, Inc.
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
package org.wildfly.migration.wildfly.full;

import org.wildfly.migration.core.Server;
import org.wildfly.migration.eap.EAP6Server;
import org.wildfly.migration.wildfly.full.from.eap6.WildFly10FullFromEAP6ServerMigration;

/**
 * @author emmartins
 */
public class WildFly10FullMigrations {

    private WildFly10FullMigrations() {
    }

    static WildFly10FullServerMigration getMigrationFrom(Server sourceServer) {
        // FIXME use service loader
        if (sourceServer instanceof EAP6Server) {
            return new WildFly10FullFromEAP6ServerMigration();
        }
        return null;
    }
}
