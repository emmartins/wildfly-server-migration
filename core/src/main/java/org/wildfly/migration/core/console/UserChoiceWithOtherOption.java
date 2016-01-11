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

import java.util.Arrays;
import java.util.Locale;

import static org.wildfly.migration.core.logger.ServerMigrationLogger.ROOT_LOGGER;

/**
 * Displays a message with a list of options, and reads the user's choice.
 * @author emmartins
 */
public class UserChoiceWithOtherOption extends UserChoice {

    private static final String OTHER = ROOT_LOGGER.other().toLowerCase(Locale.getDefault());

    private static String[] addOtherToOptions(String[] options, String otherOption) {
        final String[] optionsCopy = Arrays.copyOf(options, options.length+1);
        optionsCopy[optionsCopy.length-1] = otherOption;
        return optionsCopy;
    }

    public UserChoiceWithOtherOption(ConsoleWrapper theConsole, final String[] messageLines, final String[] options, final String otherOption, final String prompt, final ResultHandler resultHandler) {
        super(theConsole, messageLines, addOtherToOptions(options, otherOption), prompt, new ResultHandlerWrapper(theConsole, resultHandler, otherOption));
    }

    public UserChoiceWithOtherOption(ConsoleWrapper theConsole, final String message, final String[] options, final String otherOption, final String prompt, final ResultHandler resultHandler) {
        this(theConsole, message == null ? (String[]) null : new String[] { message }, options, otherOption, prompt, resultHandler);
    }

    public UserChoiceWithOtherOption(ConsoleWrapper theConsole, final String[] options, final String otherOption, final String prompt, final ResultHandler resultHandler) {
        this(theConsole, (String[]) null, options, otherOption, prompt, resultHandler);
    }

    private static class ResultHandlerWrapper implements UserChoice.ResultHandler {

        private final ConsoleWrapper theConsole;
        private final ResultHandler resultHandler;
        private final String otherChoice;

        private ResultHandlerWrapper(ConsoleWrapper theConsole, ResultHandler resultHandler, String otherChoice) {
            this.theConsole = theConsole;
            this.resultHandler = resultHandler;
            this.otherChoice = otherChoice;
        }

        @Override
        public void onChoice(String choice) {
            if (!otherChoice.equals(choice)) {
                resultHandler.onChoice(choice);
            } else {
                // user opted for "other" option, read it
                final UserInput.ResultHandler inputResultHandler = new UserInput.ResultHandler() {
                    @Override
                    public void onInput(String input) {
                        resultHandler.onOther(input);
                    }
                    @Override
                    public void onError() {
                        resultHandler.onError();
                    }
                };
                new UserInput(theConsole, (String[]) null, ROOT_LOGGER.otherChoice(), inputResultHandler).execute();
            }
        }

        @Override
        public void onError() {
            resultHandler.onError();
        }
    }

    public interface ResultHandler extends UserChoice.ResultHandler {
        void onOther(String otherChoice);
    }
}
