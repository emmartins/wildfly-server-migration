/*
 * Copyright 2017 Red Hat, Inc.
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

package org.jboss.migration.core.jboss;

import java.util.regex.Pattern;

/**
 * @author emmartins
 */
public class DeploymentOverlayLinkMatcher {

    public static boolean matches(String deploymentName, String[] overlayDeployments) {
        for (String overlayDeployment : overlayDeployments) {
            if (isWildcard(overlayDeployment)) {
                if (getPattern(overlayDeployment).matcher(deploymentName).matches()) {
                    return true;
                }
            } else {
                if (overlayDeployment.equals(deploymentName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isWildcard(String name) {
        return name.contains("*") || name.contains("?");
    }

    private static Pattern getPattern(String name) {
        return Pattern.compile(wildcardToJavaRegexp(name));
    }

    private static String wildcardToJavaRegexp(String expr) {
        if(expr == null) {
            throw new IllegalArgumentException("expr is null");
        }
        String regex = expr.replaceAll("([(){}\\[\\].+^$])", "\\\\$1"); // escape regex characters
        regex = regex.replaceAll("\\*", ".*"); // replace * with .*
        regex = regex.replaceAll("\\?", "."); // replace ? with .
        return regex;
    }
}
