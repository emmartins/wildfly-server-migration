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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * State to display a message to the user with option to confirm a choice.
 * This state handles either a yes or no outcome and will loop with an error
 * on invalid input.
 * @author emmartins
 */
public class UserConfirmation {

    // These are deliberately using the default locale i.e. the same as the language the interface is presented in.
    private static final String LONG_YES = ServerMigrationLogger.ROOT_LOGGER.yes().toLowerCase(Locale.getDefault());
    private static final String LONG_NO = ServerMigrationLogger.ROOT_LOGGER.no().toLowerCase(Locale.getDefault());
    private static final String SHORT_YES = ServerMigrationLogger.ROOT_LOGGER.shortYes().toLowerCase(Locale.getDefault());
    private static final String SHORT_NO = ServerMigrationLogger.ROOT_LOGGER.shortNo().toLowerCase(Locale.getDefault());

    private ConsoleWrapper theConsole;
    private final String[] messageLines;
    private final String prompt;
    private final ResultHandler resultHandler;

    private static final int YES = 0;
    private static final int NO = 1;
    private static final int INVALID = 2;

    public UserConfirmation(ConsoleWrapper theConsole, final String[] messageLines, final String prompt, final ResultHandler resultHandler) {
        this.theConsole = theConsole;
        this.messageLines = messageLines;
        this.prompt = prompt;
        this.resultHandler = resultHandler;
    }

    public UserConfirmation(ConsoleWrapper theConsole, final String message, final String prompt, final ResultHandler resultHandler) {
        this(theConsole, new String[] { message }, prompt, resultHandler);
    }

    public void execute() throws Exception {
        if (messageLines != null) {
            for (String message : messageLines) {
                theConsole.printf(message);
                theConsole.printf(ConsoleWrapper.NEW_LINE);
            }
        }

        theConsole.printf(prompt);
        String temp = theConsole.readLine(ConsoleWrapper.SPACE);

        switch (convertResponse(temp)) {
            case YES:
                resultHandler.onYes();
                break;
            case NO:
                resultHandler.onNo();
                break;
            default: {
                List<String> acceptedValues = new ArrayList<String>(4);
                acceptedValues.add(ServerMigrationLogger.ROOT_LOGGER.yes());
                if (ServerMigrationLogger.ROOT_LOGGER.shortYes().length() > 0) {
                    acceptedValues.add(ServerMigrationLogger.ROOT_LOGGER.shortYes());
                }
                acceptedValues.add(ServerMigrationLogger.ROOT_LOGGER.no());
                if (ServerMigrationLogger.ROOT_LOGGER.shortNo().length() > 0) {
                    acceptedValues.add(ServerMigrationLogger.ROOT_LOGGER.shortNo());
                }
                StringBuilder sb = new StringBuilder(acceptedValues.get(0));
                for (int i = 1; i < acceptedValues.size() - 1; i++) {
                    sb.append(", ");
                    sb.append(acceptedValues.get(i));
                }
                new ReportError(theConsole, ServerMigrationLogger.ROOT_LOGGER.invalidResponse(sb.toString(), acceptedValues.get(acceptedValues.size() - 1))).execute();
                resultHandler.onError();
            }
        }
    }

    private int convertResponse(final String response) {
        if (response != null) {
            String temp = response.toLowerCase(); // We now need to match on the current local.
            if (LONG_YES.equals(temp) || SHORT_YES.equals(temp)) {
                return YES;
            }

            if (LONG_NO.equals(temp) || SHORT_NO.equals(temp)) {
                return NO;
            }
        }

        return INVALID;
    }

    public interface ResultHandler {
        void onNo() throws Exception;
        void onYes() throws Exception;
        void onError() throws Exception;
    }
}
