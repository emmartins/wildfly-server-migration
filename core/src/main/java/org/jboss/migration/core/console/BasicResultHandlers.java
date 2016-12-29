/*
 * Copyright 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.migration.core.console;

/**
 * @author emmartins
 */
public class BasicResultHandlers {

    public enum Result {
        ERROR,
        NO,
        YES
    }

    public static class UserConfirmation implements org.jboss.migration.core.console.UserConfirmation.ResultHandler {

        private Result result;

        public Result getResult() {
            return result;
        }

        @Override
        public void onNo() {
            result = Result.NO;
        }

        @Override
        public void onYes() {
            result = Result.YES;
        }

        @Override
        public void onError() {
            result = Result.ERROR;
        }
    }
}
