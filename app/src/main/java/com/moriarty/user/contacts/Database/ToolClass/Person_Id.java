package com.moriarty.user.contacts.Database.ToolClass;

import android.net.Uri;

/**
 * Created by user on 16-11-7.
 */
public final class Person_Id {
    public final static String AYTHORITY="com.moriarty.user.contacts.Database.ContentProvider.PersonIdProvider";
    public final static class person_Id{
        public final static String _ID="_id";
        public final static String SOURCE_ID="Source_Id";
        public final static String TEL="Tel";
        public final static Uri PERSON_ID_URI=Uri.parse("content://"+AYTHORITY+"/person_id");
    }
}
