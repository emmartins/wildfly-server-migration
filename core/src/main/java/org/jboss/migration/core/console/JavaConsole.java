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
package org.jboss.migration.core.console;

import org.jboss.migration.core.logger.ServerMigrationLogger;

import java.io.Console;
import java.io.IOError;
import java.util.IllegalFormatException;

/**
 * {@link ConsoleWrapper} for Java System's console.
 *
 * @author <a href="mailto:flemming.harms@gmail.com">Flemming Harms</a>
 */
public class JavaConsole implements ConsoleWrapper {

    private Console theConsole = System.console();

    @Override
    public void format(String fmt, Object... args) throws IllegalFormatException {
        if (hasConsole()) {
            theConsole.format(fmt, args);
        } else {
            System.out.format(fmt, args);
        }
    }

    @Override
    public void printf(String format, Object... args) throws IllegalFormatException {
        if (hasConsole()) {
            theConsole.printf(format, args);
        } else {
            System.out.format(format, args);
        }
    }

    @Override
    public String readLine(String fmt, Object... args) throws IOError {
        if (hasConsole()) {
            return theConsole.readLine(fmt, args);
        } else {
            throw ServerMigrationLogger.ROOT_LOGGER.noConsoleAvailable();
        }
    }

    @Override
    public char[] readPassword(String fmt, Object... args) throws IllegalFormatException, IOError {
        if (hasConsole()) {
            return theConsole.readPassword(fmt, args);
        } else {
            throw ServerMigrationLogger.ROOT_LOGGER.noConsoleAvailable();
        }
    }

    @Override
    public boolean hasConsole() {
        return theConsole != null;
    }


}
