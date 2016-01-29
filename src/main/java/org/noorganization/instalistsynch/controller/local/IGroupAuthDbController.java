package org.noorganization.instalistsynch.controller.local;

import org.noorganization.instalistsynch.model.GroupAuth;

import java.util.List;

/**
 * Controller to access the group auth elements from db.
 * Created by tinos_000 on 29.01.2016.
 */
public interface IGroupAuthDbController {

    /**
     * Get all registered devices. Aka groups.
     * @return a List of all GroupAuth objects or an empty list.
     */
    List<GroupAuth> getRegisteredGroups();

    /**
     * Insert a registered group.
     * @param _groupAuth the auth object generated to be saved.
     * @return true if all went fine, false if something was wrong.
     */
    boolean insertRegisteredGroup(GroupAuth _groupAuth);

    /**
     * Removes the given groupAuth object from db.
     * @param _groupAuth the object to remove.
     * @return the number of deleted rows.
     */
    int removeRegisteredGroup(GroupAuth _groupAuth);

    /**
     * Checks if the given id is unique.
     * @param _groupAuth the groupAuth object to counter check.
     * @return true if unique else false.
     */
    boolean hasUniqueId(GroupAuth _groupAuth);

}
