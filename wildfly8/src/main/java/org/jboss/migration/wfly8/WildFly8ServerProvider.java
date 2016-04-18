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
package org.jboss.migration.wfly8;

import org.jboss.migration.core.AbstractServerProvider;
import org.jboss.migration.core.ProductInfo;
import org.jboss.migration.core.Server;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * The WildFly 8 {@link org.jboss.migration.core.ServerProvider}
 * @author emmartins
 */
public class WildFly8ServerProvider extends AbstractServerProvider {

    @Override
    protected ProductInfo getProductInfo(Path baseDir) throws IllegalArgumentException, IOException {
        final Path versionModuleMainDirPath = WildFly8Server.getModulesSystemLayersBaseDir(baseDir).resolve("org").resolve("jboss").resolve("as").resolve("version").resolve("main");
        if (!Files.isDirectory(versionModuleMainDirPath)) {
            return null;
        }
        class FileVisitor extends SimpleFileVisitor<Path> {
            private ProductInfo productInfo = null;
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String fileName = file.getFileName().toString();
                if (fileName.startsWith("wildfly-version-") && fileName.endsWith(".jar")) {
                    final JarFile jarFile = new JarFile(file.toFile());
                    final Manifest manifest = jarFile.getManifest();
                    String productName = null;
                    // just check if Implementation-Title entry has value WildFly: Version
                    if ("WildFly: Version".equals(manifest.getMainAttributes().getValue("Implementation-Title"))) {
                        productName = "WildFly";
                    }
                    if (productName == null) {
                        throw new IllegalArgumentException();
                    }
                    final String productVersion = manifest.getMainAttributes().getValue("Implementation-Version");
                    if (productVersion == null) {
                        throw new IllegalArgumentException();
                    }
                    productInfo = new ProductInfo(productName.trim(), productVersion.trim());
                    return FileVisitResult.TERMINATE;
                } else {
                    return FileVisitResult.CONTINUE;
                }
            }
        }
        final FileVisitor fileVisitor = new FileVisitor();
        Files.walkFileTree(versionModuleMainDirPath, fileVisitor);
        return fileVisitor.productInfo;
    }

    @Override
    protected String getProductNameRegex() {
        return "WildFly";
    }

    @Override
    protected String getProductVersionRegex() {
        return "8\\..*";
    }

    @Override
    protected Server constructServer(ProductInfo productInfo, Path baseDir) {
        return new WildFly8Server(productInfo, baseDir);
    }

    @Override
    public String getName() {
        return "WildFly 8.x";
    }
}
