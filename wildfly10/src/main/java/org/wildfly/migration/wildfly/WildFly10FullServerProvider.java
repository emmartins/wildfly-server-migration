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
package org.wildfly.migration.wildfly;

import org.wildfly.migration.core.AbstractServerProvider;
import org.wildfly.migration.core.ManifestProductInfo;
import org.wildfly.migration.core.ProductInfo;
import org.wildfly.migration.core.Server;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author emmartins
 */
public class WildFly10FullServerProvider extends AbstractServerProvider {

    protected ProductInfo getProductInfo(Path baseDir) throws IllegalArgumentException, IOException {
        final Path manifestPath = WildFly10FullServer.getModulesSystemLayersBaseDir(baseDir).resolve("org").resolve("jboss").resolve("as").resolve("product").resolve("wildfly-full").resolve("dir").resolve("META-INF").resolve("MANIFEST.MF");
        final ManifestProductInfo productInfo = ManifestProductInfo.from(manifestPath);
        return productInfo;
    }

    @Override
    protected String getProductNameRegex() {
        return "WildFly Full";
    }

    @Override
    protected String getProductVersionRegex() {
        return "10\\..*";
    }

    @Override
    protected Server constructServer(ProductInfo productInfo, Path baseDir) {
        return new WildFly10FullServer(productInfo, baseDir);
    }

    @Override
    public String getName() {
        return "WildFly Full 10.x";
    }
}
