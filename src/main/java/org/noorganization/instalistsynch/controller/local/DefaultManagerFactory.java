/*
 * Copyright 2016 Tino Siegmund, Michael Wodniok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.noorganization.instalistsynch.controller.local;

import org.noorganization.instalistsynch.controller.local.IAuthManagerController;
import org.noorganization.instalistsynch.controller.local.IGroupManagerController;
import org.noorganization.instalistsynch.controller.local.impl.DefaultAuthManagerController;
import org.noorganization.instalistsynch.controller.local.impl.DefaultGroupManagerController;

/**
 * Get the default manager instances.
 * Created by tinos_000 on 10.02.2016.
 */
public class DefaultManagerFactory {

    /**
     * Get the DefaultAuthManagerController instance.
     *
     * @return the DefaultAuthManagerController instance.
     */
    public static IAuthManagerController getAuthManagerController() {
        return DefaultAuthManagerController.getInstance();
    }

    /**
     * Get the DefaultGroupManagerController instance.
     *
     * @return the DefaultGroupManagerController instance.
     */
    public static IGroupManagerController getGroupManagerController() {
        return DefaultGroupManagerController.getInstance();
    }
}
