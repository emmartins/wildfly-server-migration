/*
 * Copyright 2018 Red Hat, Inc.
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
package org.jboss.migration.eap;

/**
 * The EAP 7.2 CD {@link org.jboss.migration.core.ServerProvider}.
 * @author emmartins
 */
public class EAPCDServerProvider7_2 extends EAPServerProvider7_2 {

    @Override
    protected String getProductNameRegex() {
        return "JBoss EAP CD";
    }

    @Override
    public String getName() {
        return "JBoss EAP CD 7.2";
    }
}
