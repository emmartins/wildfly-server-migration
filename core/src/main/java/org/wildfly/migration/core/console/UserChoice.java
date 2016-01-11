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

import java.util.ArrayList;
import java.util.List;

import static org.wildfly.migration.core.console.ConsoleWrapper.NEW_LINE;
import static org.wildfly.migration.core.logger.ServerMigrationLogger.ROOT_LOGGER;

/**
 * Displays a message with a list of options, and reads the user's choice.
 * @author emmartins
 */
public class UserChoice {

    protected ConsoleWrapper theConsole;
    protected final String[] messageLines;
    protected final String[] options;
    protected final String prompt;
    protected final ResultHandler resultHandler;

    private static final int DEFAULT_OPTION = 0;

    public UserChoice(ConsoleWrapper theConsole, final String[] messageLines, final String[] options, final String prompt, final ResultHandler resultHandler) {
        this.theConsole = theConsole;
        this.messageLines = messageLines;
        this.prompt = prompt;
        this.resultHandler = resultHandler;
        this.options = options;
    }

    public UserChoice(ConsoleWrapper theConsole, final String message, final String[] options, final String prompt, final ResultHandler resultHandler) {
        this(theConsole, new String[] { message }, options, prompt, resultHandler);
    }

    public void execute() {
        if (messageLines != null) {
            for (String message : messageLines) {
                theConsole.printf(message);
                theConsole.printf(NEW_LINE);
            }
        }

        for (int i=0; i < options.length; i++) {
            theConsole.printf(i+". "+options[i]);
            theConsole.printf(NEW_LINE);
        }

        theConsole.printf(prompt);

        String temp = theConsole.readLine("("+DEFAULT_OPTION+"): ");
        if (temp == null) {
            /*
             * This will return user to the command prompt so add a new line to ensure the command prompt is on the next
             * line.
             */
            theConsole.printf(NEW_LINE);
            return;
        } else {
            // default is first option, and the user may just press enter to opt for it
            int choice = DEFAULT_OPTION;
            if (!temp.equals("")) {
                // didn't opt for default
                choice = Integer.valueOf(temp);
            }
            if (choice >= 0 && choice < options.length) {
                // valid
                resultHandler.onChoice(options[choice]);
            } else {
                // invalid
                List<String> acceptedValues = new ArrayList<String>(4);
                for (int i=0; i < options.length; i++) {
                    acceptedValues.add(String.valueOf(i));
                }
                StringBuilder sb = new StringBuilder(acceptedValues.get(0));
                for (int i = 1; i < acceptedValues.size() - 1; i++) {
                    sb.append(", ");
                    sb.append(acceptedValues.get(i));
                }
                new ReportError(theConsole, ROOT_LOGGER.invalidResponse(sb.toString(), acceptedValues.get(acceptedValues.size() - 1))).execute();
                resultHandler.onError();
            }
        }
    }

    public interface ResultHandler {
        void onChoice(String choice);
        void onError();
    }
}
