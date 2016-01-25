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

import org.wildfly.migration.core.AbstractServerProvider;
import org.wildfly.migration.core.ProductInfo;
import org.wildfly.migration.core.Server;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author emmartins
 */
public abstract class AbstractTestServerProvider extends AbstractServerProvider {

    private final TestServer testServer;

    public AbstractTestServerProvider(TestServer testServer) {
        this.testServer = testServer;
    }

    @Override
    protected ProductInfo getProductInfo(Path baseDir) throws IOException {
        return TestServer.getBaseDir(testServer.getProductInfo()).equals(baseDir) ? testServer.getProductInfo() : null;
    }

    @Override
    protected String getProductNameRegex() {
        return testServer.getProductInfo().getName();
    }

    @Override
    protected String getProductVersionRegex() {
        return testServer.getProductInfo().getVersion();
    }

    @Override
    protected Server constructServer(ProductInfo productInfo, Path baseDir) {
        return testServer;
    }

    @Override
    public String getName() {
        return testServer.getProductInfo().getName();
    }
}
