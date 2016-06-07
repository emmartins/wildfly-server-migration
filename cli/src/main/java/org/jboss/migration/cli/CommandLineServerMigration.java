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
package org.jboss.migration.cli;

import org.jboss.migration.core.MigrationData;
import org.jboss.migration.core.ServerMigration;
import org.jboss.migration.core.logger.ServerMigrationLogger;
import org.jboss.migration.core.report.HtmlReportWriter;
import org.jboss.migration.core.report.XmlReportWriter;
import org.wildfly.security.manager.WildFlySecurityManager;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * The command line tool to migrate a WildFly server.
 *
 * @author Eduardo Martins
 */
public class CommandLineServerMigration {
    // Capture System.out and System.err before they are redirected by STDIO
    private static final PrintStream STDOUT = System.out;
    private static final PrintStream STDERR = System.err;

    private static void usage() {
        CommandLineArgumentUsageImpl.printUsage(STDOUT);
    }

    private CommandLineServerMigration() {
    }

    /**
     * The main method.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        try {
            if(args.length < 4) {
                usage();
                abort(null);
            }
            Path source = null;
            Path target = null;
            Path environment = null;
            Boolean interactive = null;
            for(int i = 0; i < args.length; ++i) {
                String arg = args[i];
                switch (arg) {
                    case CommandLineConstants.ENVIRONMENT: {
                        ++i;
                        if(i == args.length || environment != null) {
                            usage();
                            abort(null);
                        }
                        environment = resolvePath(args[i]);
                        break;
                    }
                    case CommandLineConstants.INTERACTIVE: {
                        ++i;
                        if(i == args.length || interactive != null) {
                            usage();
                            abort(null);
                        }
                        interactive = Boolean.valueOf(args[i]);
                        break;
                    }
                    case CommandLineConstants.SOURCE: {
                        ++i;
                        if(i == args.length || source != null) {
                            usage();
                            abort(null);
                        }
                        source = resolvePath(args[i]);
                        break;
                    }
                    case CommandLineConstants.TARGET: {
                        ++i;
                        if(i == args.length || target != null) {
                            usage();
                            abort(null);
                        }
                        target = resolvePath(args[i]);
                        break;
                    }
                }
            }
            if (interactive == null) {
                interactive = true;
            }
            Properties userEnvironment = null;
            if (environment != null) {
                try (InputStream inputStream = Files.newInputStream(environment)) {
                    userEnvironment = new Properties();
                    userEnvironment.load(inputStream);
                }
            }

            WildFlySecurityManager.setPropertyPrivileged("java.util.logging.manager", "org.jboss.logmanager.LogManager");

            final MigrationData migrationData = new ServerMigration()
                    .from(source)
                    .to(target)
                    .interactive(interactive)
                    .userEnvironment(userEnvironment)
                    .run();

            final String outputDir = System.getProperty("jboss.server.migration.output.dir");
            if (outputDir != null) {
                final Path outputDirPath = FileSystems.getDefault().getPath(outputDir);
                try {
                    XmlReportWriter.INSTANCE.writeContent(outputDirPath.resolve("migration-report.xml").toFile(), migrationData);
                } catch (Throwable e) {
                    ServerMigrationLogger.ROOT_LOGGER.error("XML Report write failed", e);
                }
                try {
                    final String configDir = System.getProperty("jboss.server.migration.config.dir");
                    final Path configDirPath = configDir != null ? FileSystems.getDefault().getPath(configDir) : outputDirPath.getParent().resolve("config");
                    HtmlReportWriter.INSTANCE.toPath(outputDirPath.resolve("migration-report.html"), migrationData, HtmlReportWriter.ReportTemplate.from(configDirPath.resolve("migration-report-template.html")));
                } catch (Throwable e) {
                    ServerMigrationLogger.ROOT_LOGGER.error("HTML Report write failed", e);
                }
            }
        } catch (Throwable t) {
            abort(t);
        }
    }

    private static void abort(Throwable t) {
        try {
            if (t != null) {
                t.printStackTrace(STDERR);
            }
        } finally {
            System.exit(1);
        }
    }

    private static Path resolvePath(String s) throws IllegalArgumentException {
        final FileSystem fileSystem = FileSystems.getDefault();
        Path path = fileSystem.getPath(s).normalize();
        Path absolutePath = path.isAbsolute() ? path : fileSystem.getPath(System.getProperty("user.dir")).resolve(path);
        if (!Files.exists(absolutePath)) {
            throw new IllegalArgumentException("File "+absolutePath+" does not exists.");
        } else {
            return absolutePath;
        }
    }
}
