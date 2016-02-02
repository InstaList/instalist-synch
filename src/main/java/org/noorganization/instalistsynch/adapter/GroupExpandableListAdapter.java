package org.noorganization.instalistsynch.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import org.noorganization.instalistsynch.model.GroupAuthAccess;
import org.noorganization.instalistsynch.model.GroupExpandableList;
import org.noorganization.instalistsynch.model.GroupMember;

import java.util.List;

/**
 * An expandable list adapter to display and manage all groups.
 * Created by tinos_000 on 02.02.2016.
 */
public class GroupExpandableListAdapter extends BaseExpandableListAdapter {

    private final List<GroupExpandableList> mGroupExpandableListSparseArray;
    private LayoutInflater mLayoutInflater;
    private Context mContext;


    public GroupExpandableListAdapter(List<GroupExpandableList> groupExpandableListSparseArray, Context context) {
        super();
        mGroupExpandableListSparseArray = groupExpandableListSparseArray;
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getGroupCount() {
        return mGroupExpandableListSparseArray.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mGroupExpandableListSparseArray.get(groupPosition).getGroupMemberList().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mGroupExpandableListSparseArray.get(groupPosition).getGroupAuthAccess();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mGroupExpandableListSparseArray.get(groupPosition).getGroupMemberList().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return mGroupExpandableListSparseArray.get(groupPosition).getGroupAuthAccess().hashCode();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return mGroupExpandableListSparseArray.get(groupPosition).getGroupMemberList().get(childPosition).hashCode();
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupAuthAccess groupAuthAccess = (GroupAuthAccess) getGroup(groupPosition);
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(android.R.layout.simple_list_item_1, null);
        }
        TextView text = (TextView) convertView.findViewById(android.R.id.text1);
        text.setText(groupAuthAccess.getDeviceId());
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        GroupMember groupMember = (GroupMember) getChild(groupPosition, childPosition);
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(android.R.layout.simple_list_item_1, null);
        }
        TextView text = (TextView) convertView.findViewById(android.R.id.text1);
        text.setText(groupMember.getName().concat(" ").concat(groupMember.isAuthorized() ? " authorized" : " not authorized"));
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
