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

/**
 * Display a message to the user and read a line of input.
 * @author emmartins
 */
public class UserInput {

    private ConsoleWrapper theConsole;
    private final String[] messageLines;
    private final String prompt;
    private final ResultHandler resultHandler;

    public UserInput(ConsoleWrapper theConsole, final String prompt, final ResultHandler resultHandler) {
        this(theConsole, null, prompt, resultHandler);
    }

    public UserInput(ConsoleWrapper theConsole, final String[] messageLines, final String prompt, final ResultHandler resultHandler) {
        this.theConsole = theConsole;
        this.messageLines = messageLines;
        this.prompt = prompt;
        this.resultHandler = resultHandler;
    }

    public void execute() {
        if (messageLines != null) {
            for (String message : messageLines) {
                theConsole.printf(message);
                theConsole.printf(ConsoleWrapper.NEW_LINE);
            }
        }

        theConsole.printf(prompt);
        String temp = theConsole.readLine(ConsoleWrapper.SPACE);
        if (temp == null) {
            /*
             * This will return user to the command prompt so add a new line to ensure the command prompt is on the next
             * line.
             */
            theConsole.printf(ConsoleWrapper.NEW_LINE);
        } else {
            if (!temp.equals("")) {
                resultHandler.onInput(temp);
            } else {
                new ReportError(theConsole, ServerMigrationLogger.ROOT_LOGGER.invalidEmptyResponse()).execute();
                resultHandler.onError();
            }
        }
    }

    public interface ResultHandler {
        void onInput(String input);
        void onError();
    }
}
