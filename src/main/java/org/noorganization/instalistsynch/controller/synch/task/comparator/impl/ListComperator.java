package org.noorganization.instalistsynch.controller.synch.task.comparator.impl;

import org.jetbrains.annotations.NotNull;
import org.noorganization.instalist.comm.message.ListInfo;
import org.noorganization.instalist.model.ShoppingList;
import org.noorganization.instalistsynch.controller.synch.task.comparator.ISynchComperator;

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
