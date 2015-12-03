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
package org.wildfly.migration.core.logger;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

/**
 * @author emmartins
 */
@MessageLogger(projectCode = "WFMIGR") //todo: proper project code?
public interface ServerMigrationLogger extends BasicLogger {

    ServerMigrationLogger ROOT_LOGGER = Logger.getMessageLogger(ServerMigrationLogger.class, ServerMigrationLogger.class.getPackage().getName());

    /**
     * Creates an exception indicating that no java.io.Console is available.
     *
     * @return a {@link IllegalStateException} for the error.
     */
    @Message(id = 1, value = "No java.io.Console available to interact with user.")
    IllegalStateException noConsoleAvailable();

    /**
     * The long value a user would enter to indicate 'yes'
     *
     * This String should be the lower case representation in the respective locale.
     *
     * @return The value a user would enter to indicate 'yes'.
     */
    @Message(id = Message.NONE, value = "yes")
    String yes();

    /**
     * The short value a user would enter to indicate 'yes'
     *
     * If no short value is available for a specific translation then only the long value will be accepted.
     *
     * This String should be the lower case representation in the respective locale.
     *
     * @return The short value a user would enter to indicate 'yes'.
     */
    @Message(id = Message.NONE, value = "y")
    String shortYes();

    /**
     * The long value a user would enter to indicate 'no'
     *
     * This String should be the lower case representation in the respective locale.
     *
     * @return The value a user would enter to indicate 'no'.
     */
    @Message(id = Message.NONE, value = "no")
    String no();

    /**
     * The short value a user would enter to indicate 'no'
     *
     * If no short value is available for a specific translation then only the long value will be accepted.
     *
     * This String should be the lower case representation in the respective locale.
     *
     * @return The short value a user would enter to indicate 'no'.
     */
    @Message(id = Message.NONE, value = "n")
    String shortNo();

    /**
     * The error message if the confirmation response is invalid.
     *
     * @return a {@link String} for the message.
     */
    @Message(id = 2, value = "Invalid response. (Valid responses are %s and %s)")
    String invalidConfirmationResponse(String firstValues, String secondValues);

    /**
     * The error message header.
     *
     * @return a {@link String} for the message.
     */
    @Message(id = Message.NONE, value = "Error")
    String errorHeader();

    /**
     * Simple yes/no prompt.
     *
     * @return a {@link String} for the message.
     */
    @Message(id = Message.NONE, value = "yes/no?")
    String yesNo();
}
