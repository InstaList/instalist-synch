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

package org.noorganization.instalistsynch.controller.synch.comparator.impl;

import org.jetbrains.annotations.NotNull;
import org.noorganization.instalist.comm.message.ListInfo;
import org.noorganization.instalist.model.ShoppingList;
import org.noorganization.instalistsynch.controller.synch.comparator.ISynchComperator;

/**
 * Comparator to compare the ShoppingList with ListInfo from server.
 * Created by Desnoo on 16.02.2016.
 */
public class ListComperator implements ISynchComperator<ShoppingList, ListInfo> {

    @Override
    public boolean compare(@NotNull ShoppingList _param1, @NotNull ListInfo _param2) {
        if (!_param1.mName.contentEquals(_param2.getName())) {
            // names are different
            return false;
        }
        if (_param1.mCategory == null && _param2.getCategoryUUID() != null)
            // list category changed.
            return false;
        if (_param1.mCategory != null && _param2.getCategoryUUID() == null)
            // list category changed
            return false;

        if((_param1.mCategory != null) && (_param2.getCategoryUUID() != null)){
            if(!_param1.mCategory.mName.contentEquals(_param2.getName()))
                return false;
        }
        // category changes are handled in category synchronization

        return true;
    }
}
