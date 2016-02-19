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
package org.wildfly.migration.wfly10.full;

import org.wildfly.migration.core.ProductInfo;
import org.wildfly.migration.core.Server;
import org.wildfly.migration.wfly10.WildFly10Server;
import org.wildfly.migration.wfly10.WildFly10ServerMigration;

import java.nio.file.Path;

/**
 * @author emmartins
 */
public class WildFly10FullServer extends WildFly10Server {

    public WildFly10FullServer(ProductInfo productInfo, Path baseDir) {
        super(productInfo, baseDir);
    }

    protected WildFly10ServerMigration getMigration(Server source) {
        return WildFly10FullServerMigrations.getMigrationFrom(source);
    }
}
