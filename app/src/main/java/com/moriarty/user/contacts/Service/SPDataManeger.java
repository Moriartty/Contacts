package com.moriarty.user.contacts.Service;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.moriarty.user.contacts.Activity.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

/**
 * Created by user on 16-11-10.
 */
public class SPDataManeger {
    private static final String currentTag="SPDataManager:";
    String sqlitePath;
    Context context;
    String TIME="time";
    String NAME="name";
    String TIEBA_ADDRESS="address";
    String TIEBA_TITLE="title";
    String TIEBA_CONTENT="content";
    String TIEBA_REPLY_CONTENT="reply_content";
    String TIEBA_TITLETXT="titletxt";
    String TIEBA_URL="url";
    String HEAD_IMG="head_img";
    String WEIBO_NAME="name";
    String WEIBO_CONTENT_TEXT="content_text";
    String WEIBO_CONTENT_IMG="content_img";
    String WEIBO_FOCUS="focus";
    String preTag="Contacts_";
    String tiebaTag="_Tieba";
    String weiboTag="_Weibo";
    public SPDataManeger(Context context){
        this.context=context;
        this.sqlitePath=context.getDatabasePath("Contacts.db3").toString();
    }

    public boolean createSPDataTableForcontact(String tableName,int flag){
        SQLiteDatabase db=SQLiteDatabase.openOrCreateDatabase(sqlitePath,null);
        try{
            switch (flag){
                case 0:
                    db.execSQL("create table "+tableName+"(_id integer primary key autoincrement, Time TEXT,Address TEXT, " +
                            "Title TEXT,Content TEXT,Reply_Content TEXT,TitleTxt TEXT, Url TEXT, Head_Img TEXT)");
                    break;
                case 1:
                    db.execSQL("create table "+tableName+"(_id integer primary key autoincrement, Time TEXT,Name TEXT, " +
                            "CONTENT_TEXT TEXT,CONTENT_PIC TEXT,FOCUS TEXT,HEAD_IMG TEXT)");
                    break;
                case 2:
                    break;
            }
            //Log.d("Moriarty","SPDataManager:"+"SPDataTable created success");
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        db.close();
        return false;
    }
    public boolean changeSPData_TableName(String preTel,String curTel){
        SQLiteDatabase db=SQLiteDatabase.openOrCreateDatabase(sqlitePath,null);
        String preTiebaTableName=preTag+preTel+tiebaTag;
        String preWeiboTableName=preTag+preTel+weiboTag;
        String curTiebaTableName=preTag+curTel+tiebaTag;
        String curWeiboTableName=preTag+curTel+weiboTag;
        try{
            db.execSQL(" ALTER TABLE "+preTiebaTableName+" RENAME TO "+curTiebaTableName);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        try{
            db.execSQL(" ALTER TABLE "+preWeiboTableName+" RENAME TO "+curWeiboTableName);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public boolean writeInWeiboTable(HashMap<String,HashMap<String,String>> weibo_map,String tel){
        HashMap<String,String> other_info=new HashMap<>();
        String tableName=preTag+tel+weiboTag;
        SQLiteDatabase db=SQLiteDatabase.openOrCreateDatabase(sqlitePath,null);
        try{
            db.execSQL("delete from "+tableName);//先删除原表中的所有内容
        }catch (Exception e){
            //如果不存在这张表，就创建一张
            createSPDataTableForcontact(tableName,1);
        }
        int size=weibo_map.size();
        Log.d("Moriarty","AddContactsService:"+size);
        String sql;
        if(size>0){
            other_info.putAll(weibo_map.get("other_info"));
            weibo_map.remove("other_info");
            ArrayList<HashMap<String,String>> weibo_list=new ArrayList<>(weibo_map.values());
            //Log.d("Moriarty","SPDataManager:"+other_info.get("name"));
            sql="insert into "+tableName +" values(null , ? , ?, ?, ?, ?, ?)";
            //Log.d("Moriarty","SPDataManager:"+weibo_list.size());
            try{
                for(HashMap<String,String> temp:weibo_list){
                    db.execSQL(sql,new String[]{temp.get(TIME),other_info.get(WEIBO_NAME),temp.get(WEIBO_CONTENT_TEXT)
                            ,temp.get(WEIBO_CONTENT_IMG),temp.get(WEIBO_FOCUS),other_info.get(HEAD_IMG)});
                }
                return true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        db.close();
        return false;
    }

    public boolean WriteInTiebaTable(HashMap<String,HashMap<String,String>> tieba_map,String tel){
        int size=tieba_map.size();
        String tableName=preTag+tel+tiebaTag;
        String sql;
        SQLiteDatabase db=SQLiteDatabase.openOrCreateDatabase(sqlitePath,null);
        try{
            db.execSQL("delete from "+tableName);//先删除原表中的所有内容
        }catch (Exception e){
            //如果不存在这张表，就创建一张
            createSPDataTableForcontact(tableName,0);
        }
        if(size>0){
            HashMap<String,String> other_info=new HashMap<>();
            other_info.putAll(tieba_map.get("other_info"));
            tieba_map.remove("other_info");
            ArrayList<HashMap<String ,String>> tieba_list=new ArrayList<>(tieba_map.values());
            sql="insert into "+tableName +" values(null , ? , ?, ?, ?, ?, ?, ?, ?)";
            try{
                for(HashMap<String,String> temp:tieba_list){
                    if(temp.containsKey(TIEBA_TITLE)){
                        db.execSQL(sql,new String[]{temp.get(TIME),temp.get(TIEBA_ADDRESS),temp.get(TIEBA_TITLE)
                                ,temp.get(TIEBA_CONTENT),"","",temp.get(TIEBA_URL),other_info.get(HEAD_IMG)});
                    }
                    else if(temp.containsKey(TIEBA_REPLY_CONTENT)){
                        db.execSQL(sql,new String[]{temp.get(TIME),temp.get(TIEBA_ADDRESS),"","",temp.get(TIEBA_TITLETXT)
                                ,temp.get(TIEBA_REPLY_CONTENT),temp.get(TIEBA_URL),other_info.get(HEAD_IMG)});
                    }
                }
                return true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        db.close();
        return false;
    }

    public HashMap<String,HashMap<String,String>> getTiebaData(String tableName,String tiebaId){
        HashMap<String,HashMap<String,String>> tieba_data=new HashMap<>();
        HashMap<String,String> other_info=new HashMap<>();
        String imgurl=null;
        Cursor cursor;
        int i=0;
        SQLiteDatabase db=SQLiteDatabase.openOrCreateDatabase(sqlitePath,null);
        try{
            cursor=db.rawQuery("select * from "+tableName,null);
        }catch (Exception e){
            return null;
        }
        while(cursor.moveToNext()){
            HashMap<String,String> temp=new HashMap<>();
            temp.put(TIME,cursor.getString(1));
            temp.put(TIEBA_ADDRESS,cursor.getString(2));
            if(cursor.getString(3)==null||cursor.getString(3).equals("")){
                temp.put(TIEBA_REPLY_CONTENT,cursor.getString(5));
                temp.put(TIEBA_TITLETXT,cursor.getString(6));
            }
            else {
                temp.put(TIEBA_TITLE,cursor.getString(3));
                temp.put(TIEBA_CONTENT,cursor.getString(4));
            }
            temp.put(TIEBA_URL,cursor.getString(7));
            imgurl=cursor.getString(8);
            tieba_data.put("note"+Integer.toString(i++),temp);
        }
        cursor.close();
        db.close();
        other_info.put(HEAD_IMG,imgurl);
        other_info.put(NAME,tiebaId);
        tieba_data.put("other_info",other_info);
        return tieba_data;
    }

    public HashMap<String,HashMap<String,String>> getWeiboData(String tableName){
        HashMap<String,HashMap<String,String>> weibo_data=new HashMap<>();
        HashMap<String,String> other_info=new HashMap<>();
        Cursor cursor;
        int i=0;
        SQLiteDatabase db=SQLiteDatabase.openOrCreateDatabase(sqlitePath,null);
        try{
            cursor=db.rawQuery("select * from "+tableName,null);
        }catch (Exception e){
            return null;
        }
        while(cursor.moveToNext()){
            HashMap<String,String> temp=new HashMap<>();
            temp.put(TIME,cursor.getString(1));
            temp.put(WEIBO_CONTENT_TEXT,cursor.getString(3));
            temp.put(WEIBO_CONTENT_IMG,cursor.getString(4));
            temp.put(WEIBO_FOCUS,cursor.getString(5));
            if(other_info.size()==0){
                other_info.put(WEIBO_NAME,cursor.getString(2));
                other_info.put(HEAD_IMG,cursor.getString(6));
            }
            weibo_data.put(Integer.toString(i++),temp);
        }
        weibo_data.put("other_info",other_info);
        cursor.close();
        db.close();
        return weibo_data;
    }
    public void deleteTiebaData(String tableName){
        SQLiteDatabase db=SQLiteDatabase.openOrCreateDatabase(sqlitePath,null);
        try {
            db.execSQL("drop table "+tableName);
        }catch (Exception e){
            Log.d(MainActivity.TAG,currentTag+"haven't this table");
        }
        db.close();
    }
    public void deleteWeiboData(String tableName){
        SQLiteDatabase db=SQLiteDatabase.openOrCreateDatabase(sqlitePath,null);
        try{
            db.execSQL("drop table "+tableName);
        }catch (Exception e){
            Log.d(MainActivity.TAG,currentTag+"haven't this table");
        }
        db.close();
    }

}
