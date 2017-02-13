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

import java.io.IOError;
import java.util.IllegalFormatException;

/**
 * Wrap the console commands
 *
 * @author <a href="mailto:flemming.harms@gmail.com">Flemming Harms</a>
 */
public interface ConsoleWrapper {

    String NEW_LINE = String.format("%n");
    String SPACE = " ";

    /**
     * Writes a formatted string to this console's output stream using
     * the specified format string and arguments.
     * see <a href="../util/Formatter.html#syntax">Format string syntax</a>
     * @param fmt format string
     * @param args the args
     * @throws IllegalFormatException if the string format is invalid
     */
    void format(String fmt, Object... args) throws IllegalFormatException;

    /**
     * A convenience method to write a formatted string to this console's
     * output stream using the specified format string and arguments.
     *
     * @param format the format string
     * @param args the args
     * @throws IllegalStateException  if the string format is invalid
     */
    void printf(String format, Object... args) throws IllegalFormatException;

    /**
     * Provides a formatted prompt, then reads a single line of text from the
     * console.
     *
     * @param fmt the format string
     * @param args the args
     * @return A string containing the line read from the console, not
     *          including any line-termination characters, or <tt>null</tt>
     *          if an end of stream has been reached.
     * @throws IOError if there is an error reading from the console
     */
    String readLine(String fmt, Object... args) throws IOError;

    /**
     * Provides a formatted prompt, then reads a password or passphrase from
     * the console with echoing disabled.
     *
     * @param fmt the format string
     * @param args the args
     * @return  A character array containing the password or passphrase read
     *          from the console.
     * @throws IOError if there is an error reading from the console
     */
    char[] readPassword(String fmt, Object... args) throws IllegalFormatException, IOError;

    /**
     * Check if the wrapper does have a console.
     *
     * @return true if the wrapper does have a console.
     */
    boolean hasConsole();
}
