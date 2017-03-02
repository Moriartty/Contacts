package com.moriarty.user.contacts.Database.DatabaseHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by user on 16-8-28.
 */
public class MyDatabaseHelper extends SQLiteOpenHelper {
    final String CREATE_TABLE_SQL1="create table person_inf(_id integer primary key autoincrement, Name varchar(50)" +
            ", GroupType varchar(50), Head_Portrait varchar(50), TiebaId varchar(50), TiebaUrl varchar(50)" +
            ", WeiboId varchar(50), WeiboUrl varchar(50), IsCollect integer)";
    final String CREATE_TABLE_SQL2="create table group_type(_id integer primary key autoincrement, Type varchar(50))";
    final String CREATE_TABLE_SQL3="create table person_id(_id integer primary key autoincrement, Tel varchar(50),Source_Id varchar(50))";
    public MyDatabaseHelper(Context context, String name, int version){
        super(context,name,null,version);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SQL1);
        db.execSQL(CREATE_TABLE_SQL2);
        db.execSQL(CREATE_TABLE_SQL3);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
