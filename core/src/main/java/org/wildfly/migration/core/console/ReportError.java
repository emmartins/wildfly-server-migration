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
package org.wildfly.migration.core.console;

import static org.wildfly.migration.core.console.ConsoleWrapper.NEW_LINE;
import static org.wildfly.migration.core.logger.ServerMigrationLogger.ROOT_LOGGER;

/**
 * Report an error to the user.
 * @author emmartins
 */
public class ReportError {

    private final String errorMessage;
    private ConsoleWrapper theConsole;

    public ReportError(ConsoleWrapper theConsole, String errorMessage) {
        this.errorMessage = errorMessage;
        this.theConsole = theConsole;
    }

    public void execute() {
        boolean direct = !theConsole.hasConsole();
        // Errors should be output in all modes.
        printf(NEW_LINE, direct);
        printf(" * ", direct);
        printf(ROOT_LOGGER.errorHeader(), direct);
        printf(" * ", direct);
        printf(NEW_LINE, direct);
        printf(errorMessage, direct);
        printf(NEW_LINE, direct);
        printf(NEW_LINE, direct);
    }

    private void printf(final String message, final boolean direct) {
        if (direct) {
            System.err.print(message);
        } else {
            theConsole.printf(message);
        }
    }

}
