package org.noorganization.instalistsynch.controller.local;

import org.noorganization.instalistsynch.model.GroupMember;

import java.util.List;

/**
 * Interface to access the group member db controller.
 * Created by tinos_000 on 02.02.2016.
 */
public interface IGroupMemberDbController {

    /**
     * Insert a GroupMember object into the database.
     * @param _groupMember the groupmember to insert
     * @return the inserted groupmember, with the created uuid. null if something failed
     */
    GroupMember insert(GroupMember _groupMember);

    /**
     * Update the exisiting groupmember entry by the given object.
     * @param _groupMember the groupmember to be updated.
     * @return true if a element was affected, else false.
     */
    boolean update(GroupMember _groupMember);

    /**
     * Delete the groupmember by its id.
     * @param _uuid the uuid of this groupmember.
     * @return true if deleted successful else false.
     */
    boolean delete(String _uuid);

    /**
     * Get a GroupMember by its id.
     * @param _uuid the uuid of the group member.
     * @return the groupmember.
     */
    GroupMember getById(String _uuid);

    /**
     * Requests a list that contains all members that are registered in the same group as this device.
     * @param _ownerId the associated local device id for this group.
     * @return a list of the found members or an empty list.
     */
    List<GroupMember> getByOwnerId(String _ownerId);

}
