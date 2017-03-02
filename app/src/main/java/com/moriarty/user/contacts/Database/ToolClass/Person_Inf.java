package com.moriarty.user.contacts.Database.ToolClass;

import android.net.Uri;

/**
 * Created by user on 16-8-27.
 */
public final class Person_Inf {
    public final static String AYTHORITY="com.moriarty.user.contacts.Database.ContentProvider.PersonInfProvider";
    public final static class person_Inf{
        public final static String _ID="_id";
        public final static String NAME="Name";
        public final static String GROUPTYPE="GroupType";
        public final static String TIEBAID="TiebaId";
        public final static String TIEBAURL="TiebaUrl";
        public final static String WEIBOID="WeiboId";
        public final static String WEIBOURL="WeiboUrl";
        public final static String ISCOLLECT="IsCollect";
        public final static String HEAD_PORTRAIT="Head_Portrait";
        public final static Uri PERSON_INF_URI=Uri.parse("content://"+AYTHORITY+"/person_inf");
    }
}
