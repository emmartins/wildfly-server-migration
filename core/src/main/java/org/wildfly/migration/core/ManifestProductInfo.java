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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Manifest;

/**
 * The {@link ProductInfo} obtained from a Manifest file.
 * @author emmartins
 */
public class ManifestProductInfo extends ProductInfo {
    private ManifestProductInfo(String name, String version) {
        super(name, version);
    }

    /**
     * Retrieves the product info from the specified's manifest inputstream.
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static ManifestProductInfo from(InputStream inputStream) throws IOException {
        final Manifest manifest = new Manifest(inputStream);
        final String productName = manifest.getMainAttributes().getValue("JBoss-Product-Release-Name");
        if (productName == null) {
            throw new IllegalArgumentException();
        }
        final String productVersion = manifest.getMainAttributes().getValue("JBoss-Product-Release-Version");
        if (productVersion == null) {
            throw new IllegalArgumentException();
        }
        return new ManifestProductInfo(productName.trim(), productVersion.trim());
    }

    /**
     * Retrieves the product info from the specified's manifest file path.
     * @param path
     * @return
     * @throws IOException
     */
    public static ManifestProductInfo from(Path path) throws IOException {
        if (!Files.isRegularFile(path)) {
            return null;
        }
        return from(Files.newInputStream(path));
    }
}
