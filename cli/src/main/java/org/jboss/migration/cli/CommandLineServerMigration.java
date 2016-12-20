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
import org.jboss.migration.core.ServerMigrationTaskResult;
import org.jboss.migration.core.env.MigrationEnvironment;
import org.jboss.migration.core.env.SystemEnvironment;
import org.jboss.migration.core.logger.ServerMigrationLogger;
import org.jboss.migration.core.report.HtmlReportWriter;
import org.jboss.migration.core.report.XmlReportWriter;
import org.wildfly.security.manager.WildFlySecurityManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

        WildFlySecurityManager.setPropertyPrivileged("java.util.logging.manager", "org.jboss.logmanager.LogManager");

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

            final String baseDir = SystemEnvironment.INSTANCE.getPropertyAsString(EnvironmentProperties.BASE_DIR);
            if (baseDir == null) {
                throw new RuntimeException("system environment does not specifies the tool's base dir");
            }
            final Path baseDirPath = Paths.get(baseDir);
            final Path configDirPath = baseDirPath.resolve("config");
            final Path outputDirPath = baseDirPath.resolve("output");

            // setup user environment
            final MigrationEnvironment userEnvironment = new MigrationEnvironment();
            final Path configDirEnvironment = configDirPath.resolve("environment.properties");
            if (Files.exists(configDirEnvironment)) {
                userEnvironment.setProperties(loadProperties(configDirEnvironment));
            }
            if (environment != null) {
                userEnvironment.setProperties(loadProperties(environment));
            }

            // run migration
            final MigrationData migrationData = new ServerMigration()
                    .from(source)
                    .to(target)
                    .interactive(interactive)
                    .userEnvironment(userEnvironment)
                    .run();

            // write reports
            final String htmlReportFileName = userEnvironment.getPropertyAsString(EnvironmentProperties.REPORT_HTML_FILE_NAME);
            final String xmlReportFileName = userEnvironment.getPropertyAsString(EnvironmentProperties.REPORT_XML_FILE_NAME);
            if (htmlReportFileName != null) {
                try {
                    final String htmlReportTemplateFileName = userEnvironment.getPropertyAsString(EnvironmentProperties.REPORT_HTML_TEMPLATE_FILE_NAME, "migration-report-template.html");
                    final Path htmlReportTemplatePath = configDirPath.resolve(htmlReportTemplateFileName);
                    HtmlReportWriter.INSTANCE.toPath(outputDirPath.resolve(htmlReportFileName), migrationData, HtmlReportWriter.ReportTemplate.from(htmlReportTemplatePath));
                } catch (Throwable e) {
                    ServerMigrationLogger.ROOT_LOGGER.error("HTML Report write failed", e);
                }
            }
            if (xmlReportFileName != null) {
                try {
                    XmlReportWriter.INSTANCE.writeContent(outputDirPath.resolve(xmlReportFileName).toFile(), migrationData);
                } catch (Throwable e) {
                    ServerMigrationLogger.ROOT_LOGGER.error("XML Report write failed", e);
                }
            }
            if (migrationData.getRootTask().getResult().getStatus() == ServerMigrationTaskResult.Status.FAIL) {
                System.exit(1);
            }
        } catch (Throwable t) {
            abort(t);
        }
    }

    private static Properties loadProperties(Path propertiesFilePath) throws IOException {
        final Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(propertiesFilePath)) {
            properties.load(inputStream);
        }
        return properties;
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
        Path path = Paths.get(s).normalize();
        Path absolutePath = path.isAbsolute() ? path : Paths.get(System.getProperty("user.dir")).resolve(path);
        if (!Files.exists(absolutePath)) {
            throw new IllegalArgumentException("File "+absolutePath+" does not exists.");
        } else {
            return absolutePath;
        }
    }
}
