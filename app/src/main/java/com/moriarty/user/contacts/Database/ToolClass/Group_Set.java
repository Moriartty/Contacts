package com.moriarty.user.contacts.Database.ToolClass;

import android.net.Uri;

/**
 * Created by user on 16-8-27.
 */
public final class Group_Set {
    public final static String AYTHORITY="com.moriarty.user.contacts.Database.ContentProvider.GroupSetProvider";
    public final static class group_Set{
        public final static String _ID="_id";
        public final static String TYPE="Type";
        public final static Uri GROUP_SET_URI=Uri.parse("content://"+AYTHORITY+"/group_set");
    }
}
