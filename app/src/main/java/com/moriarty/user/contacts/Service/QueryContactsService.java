package com.moriarty.user.contacts.Service;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import com.moriarty.user.contacts.Activity.MainActivity;
import com.moriarty.user.contacts.Database.ToolClass.Group_Set;
import com.moriarty.user.contacts.Database.ToolClass.Person_Id;
import com.moriarty.user.contacts.Database.ToolClass.Person_Inf;
import com.moriarty.user.contacts.Others.BroadcastManager;
import com.moriarty.user.contacts.R;

import java.io.ByteArrayOutputStream;
import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by user on 16-8-11.
 */
public class QueryContactsService extends Service {
    public static ArrayList<String> names = new ArrayList<String>();
    public static ArrayList<ArrayList<String>> details=new ArrayList<ArrayList<String>>();
    HashMap<String,ArrayList<String>> groupMap=new HashMap<String,ArrayList<String>>();
    HashMap<String,String> headPortrait_Map=new HashMap<>();
    static ContentResolver contentResolver;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    static Resources res;
    public class QueryContactsBinder extends Binder{
        public QueryContactsService getService(){
            return QueryContactsService.this;
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        contentResolver=getContentResolver();
        res=getResources();
        return new QueryContactsBinder();
    }
    @Override
    public void onCreate(){
        super.onCreate();
        sharedPreferences=getSharedPreferences("count",MODE_PRIVATE);  //记录应用打开的次数
        editor=sharedPreferences.edit();
    }

    public ArrayList<String> queryContactsName(){
        ArrayList<String> names=new ArrayList<>();
        Cursor cursor=getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null,null,null,null);
        while(cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));//获取联系人姓名
            names.add(name);
        }
        boolean read=sharedPreferences.getBoolean("flag",false);
        if(read==false){
            Invalidate(names);  //如果是第一次安装应用并打开，把手机中原有的联系人数据写入自定义数据库中。
            editor.putBoolean("flag",true);
            editor.commit();
        }
        cursor.close();
        new BroadcastManager().sendBroadcast(this,2);
        return names;
    }

    public void Invalidate(ArrayList<String> names){
        for(int i=0;i<names.size();i++){
            String[] temp=new String[7];
            temp[0]=names.get(i);
            temp[1]=getResources().getString(R.string.default_group);
            temp[2]="None";
            temp[3]="";
            temp[4]="";
            temp[5]="";
            temp[6]="";
            insertData(temp);
        }
    }
    private void insertData(String[] temp){
        ContentValues values=new ContentValues();
        values.put(Person_Inf.person_Inf.NAME,temp[0]);
        values.put(Person_Inf.person_Inf.GROUPTYPE,temp[1]);
        values.put(Person_Inf.person_Inf.HEAD_PORTRAIT,temp[2]);
        values.put(Person_Inf.person_Inf.TIEBAID,temp[3]);
        values.put(Person_Inf.person_Inf.TIEBAURL,temp[4]);
        values.put(Person_Inf.person_Inf.WEIBOID,temp[5]);
        values.put(Person_Inf.person_Inf.WEIBOURL,temp[6]);
        values.put(Person_Inf.person_Inf.ISCOLLECT,0);
        contentResolver.insert(Person_Inf.person_Inf.PERSON_INF_URI,values);
    }

    public HashMap<String,ArrayList<String>> queryForGroup(){
        ArrayList<String> tempName=new ArrayList<String>();
        ArrayList<String> tempGroup=new ArrayList<String>();     //记录person_inf表中GroupType数据
        ArrayList<String> tempAllgroup=new ArrayList<String>();  //记录group_type表中group

        //查询person_inf中所有Name
        Cursor nameCursor=contentResolver.query(Person_Inf.person_Inf.PERSON_INF_URI,new String[]{"Name"},null,null,null);
        //查询person_inf中所有GroupType
        Cursor groupTypeCursor=contentResolver.query(Person_Inf.person_Inf.PERSON_INF_URI,new String[]{"GroupType"},null,null,null);
        getSQLiteData(nameCursor,tempName);
        getSQLiteData(groupTypeCursor,tempGroup);

        tempAllgroup.addAll(getAllgroup());
        //这里的groupMap主要用于Category中expandableListView的数据，因此必须获取到全部分组
        for(int i=0;i<tempAllgroup.size();i++){
            ArrayList<String> temp=new ArrayList<String>();
            groupMap.put(tempAllgroup.get(i),temp);
        }           //先将group_type表中的数据作为key,然后用person_inf中的name进行value插入
        for(int i=0;i<tempName.size();i++){
            if(groupMap.containsKey(tempGroup.get(i))){
                ArrayList<String> temp=new ArrayList<String>();
                temp=groupMap.get(tempGroup.get(i));
                temp.add(tempName.get(i));
                groupMap.put(tempGroup.get(i),temp);
            }
            else{
                ArrayList<String> newGroup=new ArrayList<String>();
                newGroup.add(tempName.get(i));
                groupMap.put(tempGroup.get(i), newGroup);
            }
        }
        return groupMap;
    }

    public static ArrayList<HashMap<String,String>> getReserveData(Context context){
        ArrayList<HashMap<String,String>> reserveData=new ArrayList<>();
        Cursor nameCursor=context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null,null,null,null);
        while(nameCursor.moveToNext()) {
            HashMap<String,String> recode=new HashMap<>();
            String name = nameCursor.getString(nameCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));//获取联系人姓名
            String phoneNumber=queryTel(name,context);
            String tiebaUrl=getTiebaUrl(name);
            String weibourl=getWeiboUrl(name);
            String renrenUrl=getRenrenUrl(name);
            String uuid=getTargetUUID(phoneNumber);

            recode.put("name",name);
            recode.put("phone",phoneNumber);
            recode.put("tiebaurl",tiebaUrl);
            recode.put("weibourl",weibourl);
            recode.put("renrenurl",renrenUrl);
            recode.put("source_id",uuid);
            reserveData.add(recode);
        }
        return reserveData;
    }

    public static String queryTel(String personName,Context context){  //如果系统数据库中没有这个人，则没有返回值
        String telNumber=null;
        Cursor cursor=context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null,"display_name=?",new String[]{personName},null);
        cursor.moveToNext();
        String ContactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));   //获取联系人id
        Cursor phones=context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID+"="+ContactId,null,null);
        while(phones.moveToNext())
        {
            telNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }
        phones.close();
        return telNumber;
    }

    private static void getSQLiteData(Cursor cursor,ArrayList<String> temp){  //读取数据的关键函数
        while(cursor.moveToNext()){
            temp.add(cursor.getString(0));
        }
    }


    private ArrayList<String> getAllgroup(){
        ArrayList<String> tempAllGroup=new ArrayList<>();
        Cursor groupTypeCursor=contentResolver.query(Group_Set.group_Set.GROUP_SET_URI,new String[]{"Type"},null,null,null);
        getSQLiteData(groupTypeCursor,tempAllGroup);
        //如果数据库是新建的，不包含defaultfroup，则将defaultgroup插入数据库中group_type表
        if(!tempAllGroup.contains(getString(R.string.default_group))){
            ContentValues values=new ContentValues();
            values.put(Group_Set.group_Set.TYPE,getString(R.string.default_group));
            contentResolver.insert(Group_Set.group_Set.GROUP_SET_URI,values);
            tempAllGroup.clear();
            groupTypeCursor=contentResolver.query(Group_Set.group_Set.GROUP_SET_URI,new String[]{"Type"},null,null,null);
            getSQLiteData(groupTypeCursor,tempAllGroup);
        }
        groupTypeCursor.close();
        return tempAllGroup;
    }

    public static ArrayList<String> getAllGroup(){        //为选择分组，删除分组，合并分组提供group数据
        ArrayList<String> tempAllGroup=new ArrayList<>();
        Cursor groupTypeCursor=contentResolver.query(Group_Set.group_Set.GROUP_SET_URI,new String[]{"Type"},null,null,null);
        getSQLiteData(groupTypeCursor,tempAllGroup);
        groupTypeCursor.close();
        return tempAllGroup;
    }

    public HashMap<String,String> queryForHeadPortrait(){
        ArrayList<String> tempName=new ArrayList<>();
        ArrayList<String> tempPath=new ArrayList<>();
        Cursor nameCursor=contentResolver.query(Person_Inf.person_Inf.PERSON_INF_URI,new String[]{"Name"},null,null,null);
        Cursor headCursor=contentResolver.query(Person_Inf.person_Inf.PERSON_INF_URI,new String[]{"Head_Portrait"},null,null,null);
        getSQLiteData(nameCursor,tempName);
        getSQLiteData(headCursor,tempPath);
        for(int i=0;i<tempName.size();i++){
            headPortrait_Map.put(tempName.get(i),tempPath.get(i));
        }
        return headPortrait_Map;
    }

    public ArrayList<String> queryForCollectedInfo(){
        ArrayList<String> tempName=new ArrayList<>();
        Cursor cursor=contentResolver.query(Person_Inf.person_Inf.PERSON_INF_URI,new String[]{"Name"}
                ,"IsCollect=1",null,null);
        while(cursor.moveToNext()){
            tempName.add(cursor.getString(0));
        }
        return tempName;
    }

    public static String getPosition_Person(String personName){
        String groupName="";
        Cursor groupTypeCursor=contentResolver.query(Person_Inf.person_Inf.PERSON_INF_URI,new String[]{"GroupType"},
                "Name=?",new String[]{personName},null);
        while(groupTypeCursor.moveToNext()){
            groupName=groupTypeCursor.getString(0);
        }
        return groupName;
    }

    public static String getHeadPicPath_Person(String personName){
        Cursor headCursor=contentResolver.query(Person_Inf.person_Inf.PERSON_INF_URI,new String[]{"Head_Portrait"},
                "Name=?",new String[]{personName},null);
        String pathTemp="";
        while(headCursor.moveToNext()){
           // Log.d("moriarty",":"+personName+":"+headCursor.getString(0));
            pathTemp=headCursor.getString(0);
        }
        headCursor.close();
        return pathTemp;
    }
    public static String getTiebaId(String personName){
        Cursor tiebaIdCursor=contentResolver.query(Person_Inf.person_Inf.PERSON_INF_URI,new String[]{"TiebaId"}
                ,"Name=?",new String[]{personName},null);
        String tiebaId="";
        while(tiebaIdCursor.moveToNext()){
            tiebaId=tiebaIdCursor.getString(0);
        }
        tiebaIdCursor.close();
        return tiebaId;
    }
    public static String getTiebaUrl(String personName){
        Cursor tiebaUrlCursor=contentResolver.query(Person_Inf.person_Inf.PERSON_INF_URI,new String[]{"TiebaUrl"}
                ,"Name=?",new String[]{personName},null);
        String tiebaUrl="";
        while (tiebaUrlCursor.moveToNext()){
            tiebaUrl=tiebaUrlCursor.getString(0);
        }
        tiebaUrlCursor.close();
        return tiebaUrl;
    }
    public static String getWeiboId(String personName){
        Cursor weiboIdCursor=contentResolver.query(Person_Inf.person_Inf.PERSON_INF_URI,new String[]{"WeiboId"}
                ,"Name=?",new String[]{personName},null);
        String weiboId="";
        while (weiboIdCursor.moveToNext()){
            weiboId=weiboIdCursor.getString(0);
        }
        weiboIdCursor.close();
        return weiboId;
    }
    public static String getWeiboUrl(String personName){
        Cursor weiboUrlCursor=contentResolver.query(Person_Inf.person_Inf.PERSON_INF_URI,new String[]{"WeiboUrl"}
                ,"Name=?",new String[]{personName},null);
        String weiboUrl="";
        while (weiboUrlCursor.moveToNext()){
            weiboUrl=weiboUrlCursor.getString(0);
        }
        weiboUrlCursor.close();
        return weiboUrl;
    }
    public static String getRenrenId(String personName){
        return "";
    }
    public static String getRenrenUrl(String personName){
        return "";
    }
    public static String getTargetUUID(String tel){
        Cursor idCursor=contentResolver.query(Person_Id.person_Id.PERSON_ID_URI,new String[]{"Source_Id"}
                ,"Tel=?",new String[]{tel},null);
        String source_id="";
        while (idCursor.moveToNext()){
            source_id=idCursor.getString(0);
        }
        idCursor.close();
        return source_id;
    }

    public static Drawable GetDrawable(String uri){
        try{
            Bitmap bitmap= MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(uri));
            BitmapDrawable drawable=new BitmapDrawable(bitmap);
            return drawable;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static String getPath(Uri uri){
        String[] projection={MediaStore.Images.Media.DATA};
        Cursor cursor=contentResolver.query(uri,projection,null,null,null);
        int column_index=cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    public static boolean IsCollected(String personName){
        Cursor cursor=contentResolver.query(Person_Inf.person_Inf.PERSON_INF_URI,new String[]{"IsCollect"}
                ,"Name=?",new String[]{personName},null);
        Integer temp=0;
        while (cursor.moveToNext()){
            temp=cursor.getInt(0);
        }
        cursor.close();
        if(temp==1)
            return true;
        else
            return false;
    }

    public static ArrayList<String> getMyInformation(SharedPreferences myInfo){
        ArrayList<String> myInfoList=new ArrayList<>();
        //myInfoList.clear();  //在每次往集合中写数据前都先将集合清空
       // myInfoList.add(myInfo.getString("my_name",res.getString(R.string.toolbar_my_name_hint)));
        myInfoList.add(myInfo.getString("my_name",null));
        myInfoList.add(myInfo.getString("my_phone",null));
        myInfoList.add(myInfo.getString("my_email",null));
        myInfoList.add(myInfo.getString("my_headrait","None"));
        myInfoList.add(myInfo.getString("my_tiebaid",""));
        myInfoList.add(myInfo.getString("my_tiebaurl",""));
        myInfoList.add(myInfo.getString("my_weiboid",""));
        myInfoList.add(myInfo.getString("my_weibourl",""));
        myInfoList.add(myInfo.getString("source_id",""));
        Log.d("Moriarty","QueryS:"+myInfoList.get(8));
        return  myInfoList;
    }
    public static HashMap<String,String> getAllMyInfoToMap(SharedPreferences myInfo){  //这是用来重写我的信息的数据
        HashMap<String,String> recode=new HashMap<>();
        recode.put("name",myInfo.getString("my_name",myInfo.getString("my_phone",null)));
        recode.put("phone",myInfo.getString("my_phone",null));
        recode.put("email",myInfo.getString("my_email",""));
        recode.put("headrait",myInfo.getString("my_headrait","None"));
        recode.put("tiebaid",myInfo.getString("my_tiebaid",""));
        recode.put("tiebaurl",myInfo.getString("my_tiebaurl",""));
        recode.put("weiboid",myInfo.getString("my_weiboid",""));
        recode.put("weibourl",myInfo.getString("my_weibourl",""));
        recode.put("source_id","");
        return recode;
    }


    public static HashMap<String,String> getMyInfoToMap(SharedPreferences myInfo){  //这是要传到服务器上的数据
        HashMap<String,String> recode=new HashMap<>();
        recode.put("name",myInfo.getString("my_name",myInfo.getString("my_phone",null)));
        recode.put("phone",myInfo.getString("my_phone",null));
        recode.put("tiebaurl",myInfo.getString("my_tiebaurl",""));
        recode.put("weibourl",myInfo.getString("my_weibourl",""));
        recode.put("renrenurl","");
        recode.put("source_id",myInfo.getString("source_id",""));
        return recode;
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
    }
}
