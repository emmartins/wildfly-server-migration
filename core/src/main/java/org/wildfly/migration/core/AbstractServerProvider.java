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
import java.util.regex.Pattern;

/**
 * Abstract {@link ServerProvider} impl that uses regex patterns to check if a product info, obtained from based dir, matches a provided server.
 * @author emmartins
 */
public abstract class AbstractServerProvider implements ServerProvider {

    @Override
    public Server getServer(Path baseDir) throws IOException {
        final ProductInfo productInfo = getProductInfo(baseDir);
        return isProviderFor(productInfo) ? constructServer(productInfo, baseDir) : null;
    }

    /**
     * Retrieves the {@link ProductInfo} from the specified base dir.
     * @param baseDir the server's base dir
     * @return
     * @throws IOException if the product's info failed to be retrieved.
     */
    protected abstract ProductInfo getProductInfo(Path baseDir) throws IOException;

    protected boolean isProviderFor(ProductInfo productInfo) {
        if (productInfo == null) {
            return false;
        }
        final String productName = productInfo.getName();
        if (productName == null || !Pattern.matches(getProductNameRegex(), productName)) {
            ServerMigrationLogger.ROOT_LOGGER.debugf("Product name %s doesn't match!", productName);
            return false;
        }
        final String productVersion = productInfo.getVersion();
        if (productVersion == null || !Pattern.matches(getProductVersionRegex(), productVersion)) {
            ServerMigrationLogger.ROOT_LOGGER.debugf("Product version %s doesn't match!", productVersion);
            return false;
        }
        return true;
    }

    /**
     * Retrieves the pattern to match product's name.
     * @return
     */
    protected abstract String getProductNameRegex();

    /**
     * Retrieves the pattern to match product's version.
     * @return
     */
    protected abstract String getProductVersionRegex();

    /**
     * Constructs the server, from specified product info and base dir's path.
     * @param productInfo the server's product info
     * @param baseDir the server's base dir
     * @return the contructed server
     */
    protected abstract Server constructServer(ProductInfo productInfo, Path baseDir);
}
