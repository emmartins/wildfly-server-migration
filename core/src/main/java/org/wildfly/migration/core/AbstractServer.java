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
package org.wildfly.migration.core;

import org.wildfly.migration.core.logger.ServerMigrationLogger;

import java.io.IOException;
import java.nio.file.Path;

/**
 * An abstract {@link Server} impl, which is usable only as migration source.
 * @author emmartins
 */
public abstract class AbstractServer implements Server {
    private final Path baseDir;
    private final ProductInfo productInfo;

    public AbstractServer(ProductInfo productInfo, Path baseDir) {
        this.productInfo = productInfo;
        this.baseDir = baseDir;
    }

    @Override
    public Path getBaseDir() {
        return baseDir;
    }

    @Override
    public ProductInfo getProductInfo() {
        return productInfo;
    }

    @Override
    public void migrate(Server source, ServerMigrationContext context) throws IOException {
        throw ServerMigrationLogger.ROOT_LOGGER.doesNotSupportsMigration(productInfo.getName(), productInfo.getVersion(), source.getProductInfo().getName(), source.getProductInfo().getVersion());
    }
}
