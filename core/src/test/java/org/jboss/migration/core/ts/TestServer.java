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
package org.jboss.migration.core.ts;

import org.jboss.migration.core.AbstractServer;
import org.jboss.migration.core.ProductInfo;
import org.jboss.migration.core.Server;
import org.jboss.migration.core.ServerMigrationTaskContext;
import org.jboss.migration.core.ServerMigrationTaskResult;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

/**
 * @author emmartins
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
        super(productInfo.getName(), productInfo, getBaseDir(productInfo), null);
        this.supportedMigrations = supportedMigrations;
    }

    @Override
    public ServerMigrationTaskResult migrate(Server source, ServerMigrationTaskContext context) throws IllegalArgumentException {
        if (!supportedMigrations.contains(source.getProductInfo())) {
            return super.migrate(source, context);
        }
        return ServerMigrationTaskResult.SUCCESS;
    }

    @Override
    public Path resolvePath(String path) {
        return null;
    }
}
