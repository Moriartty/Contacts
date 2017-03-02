package com.moriarty.user.contacts.Service;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.widget.Toast;

import com.moriarty.user.contacts.Activity.AddContacts;
import com.moriarty.user.contacts.Activity.MainActivity;
import com.moriarty.user.contacts.Database.DatabaseHelper.MyDatabaseHelper;
import com.moriarty.user.contacts.Database.ToolClass.Person_Id;
import com.moriarty.user.contacts.Database.ToolClass.Person_Inf;
import com.moriarty.user.contacts.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.jar.Attributes;
import java.util.regex.Pattern;

/**
 * Created by user on 16-8-9.
 */
public class AddContactsService extends Service{
    boolean isImptyFlag=false;
    ContentResolver contentResolver;
    static Resources res;
    static Pattern pattern = Pattern.compile("[0-9]*");
    private final static String currentTag="AddContactsService:";

    public class AddContactsBinder extends Binder{
        public AddContactsService getService(){
            return AddContactsService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        contentResolver=getContentResolver();
        res=getResources();
        return new AddContactsBinder();
    }

    public boolean addIntoContentProvide() {
        if (!isCorrect(this)) {
            return false;
        } else {
            ContentResolver resolver = getContentResolver();
            ContentValues values = new ContentValues();
            // 首先向RawContacts.CONTENT_URI执行一个空值插入，目的是获取系统返回的rawContactId
            Uri uri = resolver.insert(ContactsContract.RawContacts.CONTENT_URI, values);
            long id = ContentUris.parseId(uri);
            Log.d(MainActivity.TAG,currentTag+id);

            values.clear();// 往data表入姓名数据
            values.put(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, id);
            values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
            values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, AddContacts.Card.getName());
            resolver.insert(android.provider.ContactsContract.Data.CONTENT_URI, values);

            values.clear();//往data表写入电话数据
            values.put(ContactsContract.Data.RAW_CONTACT_ID, id);
            values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, AddContacts.Card.getPhone());
            values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
            resolver.insert(android.provider.ContactsContract.Data.CONTENT_URI, values);

            values.clear();//往data表写入email数据
            values.put(ContactsContract.Data.RAW_CONTACT_ID, id);
            values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
            values.put(ContactsContract.CommonDataKinds.Email.DATA, AddContacts.Card.getEmail());   //设置联系人email地址
            values.put(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK);   //设置电子邮件的类型
            resolver.insert(android.provider.ContactsContract.Data.CONTENT_URI, values);

            Toast.makeText(getApplicationContext(), getString(R.string.addcontact_success), Toast.LENGTH_SHORT).show();
            return true;
        }
    }
    public boolean updateContactInContentProvider(){
       // Log.d("Moriarty","AddContactsService:"+"updataContactInContentProvider");
        if (!isCorrect(this)) {
            return false;
        } else {
            ContentResolver resolver = getContentResolver();
            ContentValues values = new ContentValues();

            Cursor cursor=getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null,
                    "display_name=?",new String[]{AddContacts.history.get(0)},null);
            Log.d(MainActivity.TAG,currentTag+"historyName="+AddContacts.history.get(0));
            cursor.moveToNext();
            String ContactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));   //获取联系人id
            Log.d(MainActivity.TAG,currentTag+"ContactId="+ContactId);

            Cursor cursor2=getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,null,
                    ContactsContract.RawContacts.CONTACT_ID+"=?",new String[]{ContactId},null);
            String row_ContactId="";
            if(cursor2.moveToFirst())
                row_ContactId=cursor2.getString(cursor2.getColumnIndex(ContactsContract.RawContacts._ID));
            Log.d(MainActivity.TAG,currentTag+"row_ContactId="+row_ContactId);
            String where=ContactsContract.Data.RAW_CONTACT_ID+"=?";
            String where1=ContactsContract.Data.MIMETYPE+"=?";

            values.clear();// 往data表入姓名数据
            values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, AddContacts.Card.getName());
            resolver.update(android.provider.ContactsContract.Data.CONTENT_URI, values,where+" And "+where1,
                    new String[]{row_ContactId,ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE});

            values.clear();//往data表写入电话数据
            values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, AddContacts.Card.getPhone());
            //Log.d("Moriarty","AddContactsService:"+AddContacts.Card.getPhone());
            values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
            resolver.update(android.provider.ContactsContract.Data.CONTENT_URI, values,where+" And "+where1,new String[]{row_ContactId,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE});

            values.clear();//往data表写入email数据
            values.put(ContactsContract.CommonDataKinds.Email.DATA, AddContacts.Card.getEmail());   //设置联系人email地址
            values.put(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK);   //设置电子邮件的类型
            resolver.update(android.provider.ContactsContract.Data.CONTENT_URI, values,where+" And "+where1,new String[]{row_ContactId,
                    ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE});
            cursor.close();
            cursor2.close();
            Toast.makeText(getApplicationContext(), getString(R.string.addcontact_success), Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    public static boolean isCorrect(Context context){
        if (AddContacts.phone.getText().toString().equals("")){
            Toast.makeText(context, context.getString(R.string.noedit_tips1), Toast.LENGTH_SHORT).show();
            return false;
        }
        else{
            //这里用于判断输入的电话号码的格式是否全为数字。
            //关于正则表达式：+表示1个或多个（如"3"或"225"），*表示0个或多个（[0-9]*）（如""或"1"或"22"），?表示0个或1个([0-9]?)(如""或"7")
            if(pattern.matcher(AddContacts.phone.getText().toString()).matches())
                return true;
            else
            {
                Toast.makeText(context,context.getString(R.string.noedit_tips2),Toast.LENGTH_SHORT).show();
                return false;
            }
        }
    }
    public boolean addIntoSQLite(){
        if (isCorrect(this)){
            String[] temp=new String[7];
            temp[0]=AddContacts.Card.getName();
            temp[1]=AddContacts.Card.getGroup();
            if(!AddContacts.Card.getHeadPortrait().equals(""))
                temp[2]=AddContacts.Card.getHeadPortrait();
            else
                temp[2]="None";
            temp[3]=AddContacts.Card.getTiebaId();
            temp[4]=AddContacts.Card.getTiebaUrl();
            temp[5]=AddContacts.Card.getWeiboId();
            temp[6]=AddContacts.Card.getWeiboUrl();
            insertData(temp);
            return true;
        }
        else {
            return false;
        }
    }

    public boolean updateContactInSQLite(){   //更新自定义数据库中相关联系人字段信息
        if(isCorrect(this)){
            Cursor cursor=getContentResolver().query(Person_Inf.person_Inf.PERSON_INF_URI,null,
                    Person_Inf.person_Inf.NAME+"=?",new String[]{AddContacts.history.get(0)},null);
            cursor.moveToNext();
            String Id = cursor.getString(cursor.getColumnIndex(Person_Inf.person_Inf._ID));   //获取联系人id
            ContentValues values=new ContentValues();
            values.put(Person_Inf.person_Inf.NAME,AddContacts.Card.getName());
            values.put(Person_Inf.person_Inf.GROUPTYPE,AddContacts.Card.getGroup());
            values.put(Person_Inf.person_Inf.HEAD_PORTRAIT,AddContacts.Card.getHeadPortrait());
            values.put(Person_Inf.person_Inf.TIEBAID,AddContacts.Card.getTiebaId());
            values.put(Person_Inf.person_Inf.TIEBAURL,AddContacts.Card.getTiebaUrl());
            values.put(Person_Inf.person_Inf.WEIBOID,AddContacts.Card.getWeiboId());
            values.put(Person_Inf.person_Inf.WEIBOURL,AddContacts.Card.getWeiboUrl());
            getContentResolver().update(Person_Inf.person_Inf.PERSON_INF_URI,values,Person_Inf.person_Inf._ID+"=?",new String[]{Id});
            return true;
        }
        else
            return false;
    }

    public static boolean addIntoPerson_Id(String tel,String source_id,Context context){
        try {
            ContentValues values=new ContentValues();
            values.put(Person_Id.person_Id.TEL,tel.replace("C",""));
            values.put(Person_Id.person_Id.SOURCE_ID,source_id);
            context.getContentResolver().insert(Person_Id.person_Id.PERSON_ID_URI,values);
            return true;
        }catch (Exception e){
            Log.d(MainActivity.TAG,currentTag+"add to person_id is failed");
        }
        return false;
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

    public static boolean writeInPreference(SharedPreferences sharedPreferences, SharedPreferences.Editor editor, Context context,String source_id){  //将个人信息写入sharedPreference
        if(isCorrect(context)){
            try{
                editor.putString("my_name", AddContacts.Card.getName());
                editor.putString("my_phone", AddContacts.Card.getPhone());
                editor.putString("my_email", AddContacts.Card.getEmail());
                editor.putString("my_headrait", AddContacts.Card.getHeadPortrait());
                editor.putString("my_tiebaid", AddContacts.Card.getTiebaId());
                editor.putString("my_tiebaurl", AddContacts.Card.getTiebaUrl());
                editor.putString("my_weiboid", AddContacts.Card.getWeiboId());
                editor.putString("my_weibourl", AddContacts.Card.getWeiboUrl());
                editor.putString("source_id",source_id);
                editor.commit();
                return true;
            }catch (Exception e){
                Toast.makeText(context,res.getString(R.string.write_in_myself_failed),Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }

    public static boolean rewritePreference(HashMap<String,String> recode_me,SharedPreferences.Editor editor,Context context){
        try{
            editor.putString("my_name", recode_me.get("name"));
            editor.putString("my_phone", recode_me.get("phone"));
            editor.putString("my_email", recode_me.get("email"));
            editor.putString("my_headrait", recode_me.get("headrait"));
            editor.putString("my_tiebaid", recode_me.get("tiebaid"));
            editor.putString("my_tiebaurl", recode_me.get("tiebaurl"));
            editor.putString("my_weiboid", recode_me.get("weiboid"));
            editor.putString("my_weibourl", recode_me.get("weibourl"));
            editor.putString("source_id",recode_me.get("source_id"));
            editor.commit();
            return true;
        }catch (Exception e){
            Toast.makeText(context,res.getString(R.string.write_in_myself_failed),Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public static boolean update_CollectedInfo(String personName,int flag,ContentResolver contentResolver){
        Log.d(MainActivity.TAG,currentTag+personName);
        String Id=null;
        if(contentResolver==null){
            Log.d(MainActivity.TAG,currentTag+"sContentResolver is null");
        }
        Cursor cursor=contentResolver.query(Person_Inf.person_Inf.PERSON_INF_URI,null,
                Person_Inf.person_Inf.NAME+"=?",new String[]{personName},null);
        cursor.moveToNext();
        try{
            Id = cursor.getString(cursor.getColumnIndex(Person_Inf.person_Inf._ID));   //获取联系人id
        }catch (NullPointerException e){
            Log.d(MainActivity.TAG,currentTag+"该联系人不在自定义数据库中");
        }

        ContentValues values=new ContentValues();
        values.put(Person_Inf.person_Inf.ISCOLLECT,flag);
        contentResolver.update(Person_Inf.person_Inf.PERSON_INF_URI,values,Person_Inf.person_Inf._ID+"=?",new String[]{Id});
        return true;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

}
