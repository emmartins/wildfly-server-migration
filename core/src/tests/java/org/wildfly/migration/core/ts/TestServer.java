/*
 * Copyright 2016 Red Hat, Inc.
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
package org.wildfly.migration.core.ts;

import org.wildfly.migration.core.AbstractServer;
import org.wildfly.migration.core.ProductInfo;
import org.wildfly.migration.core.Server;
import org.wildfly.migration.core.ServerMigrationContext;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

/**
 * Created by emmartins on 18/01/16.
 */
public class TestServer extends AbstractServer {

    static Path getBaseDir(ProductInfo productInfo) {
        return Paths.get(productInfo.getName(), productInfo.getVersion());
    }

    /**
     * the product infos of source servers which migration from is supported
     */
    private final Set<ProductInfo> supportedMigrations;

    /**
     *
     * @param productInfo
     * @param supportedMigrations
     */
    public TestServer(ProductInfo productInfo, Set<ProductInfo> supportedMigrations) {
        super(productInfo, getBaseDir(productInfo));
        this.supportedMigrations = supportedMigrations;
    }

    @Override
    public void migrate(Server source, ServerMigrationContext context) throws IOException {
        if (!supportedMigrations.contains(source.getProductInfo())) {
            super.migrate(source, context);
        }
    }
}
