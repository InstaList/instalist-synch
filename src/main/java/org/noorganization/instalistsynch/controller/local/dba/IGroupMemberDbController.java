package org.noorganization.instalistsynch.controller.local.dba;

import android.database.Cursor;
import android.support.annotation.NonNull;

import org.noorganization.instalistsynch.model.GroupMember;
import org.noorganization.instalistsynch.utils.eSORT_MODE;

import java.util.List;

/**
 * Interface to access the group member db controller.
 * Created by tinos_000 on 02.02.2016.
 */
public interface IGroupMemberDbController {


    /**
     * Insert a GroupMember object into the database.
     *
     * @param _groupMember the groupmember to insert
     * @return the inserted groupmember, with the created uuid. null if something failed
     */
    GroupMember insert(@NonNull GroupMember _groupMember);

    /**
     * Update the exisiting groupmember entry by the given object.
     *
     * @param _groupMember the groupmember to be updated.
     * @return true if a element was affected, else false.
     */
    boolean update(@NonNull GroupMember _groupMember);

    /**
     * Delete the groupmember by its id.
     *
     * @param _groupId  the uuid of this groupmember.
     * @param _deviceId the id of the device.
     * @return true if deleted successful else false.
     */
    boolean delete(int _groupId, int _deviceId);

    /**
     * Get a GroupMember by its id.
     *
     * @param _groupId  the uuid of the group member.
     * @param _deviceId the id of the device.
     * @return the groupmember.
     */
    GroupMember getById(int _groupId, int _deviceId);

    /**
     * Requests a list that contains all members that are registered in the same group as this device.
     *
     * @param _groupId the associated local group id for this group.
     * @return a list of the found members or an empty list.
     */
    List<GroupMember> getByGroup(int _groupId);

    /**
     * Requests a cursor that contains all members that are registered in the given groupId.
     * Remember to close the cursor when done
     *
     * @param _groupId the associated group id for this group.
     * @return a list of the found members or an empty list.
     */
    Cursor getCursorByGroup(int _groupId);

    /**
     * Get the cursor to all groupmembers. They will be grouped by their groupid.
     * Remember to close the cursor when done
     *
     * @param _sortMode the mode how to sort the entries.
     * @return a cursor to all groupmembers
     */
    Cursor getAllGroupMembersByGroup(eSORT_MODE _sortMode);

}