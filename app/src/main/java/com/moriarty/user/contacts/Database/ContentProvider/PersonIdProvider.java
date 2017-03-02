package com.moriarty.user.contacts.Database.ContentProvider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.moriarty.user.contacts.Database.DatabaseHelper.MyDatabaseHelper;
import com.moriarty.user.contacts.Database.ToolClass.Person_Id;
import com.moriarty.user.contacts.Database.ToolClass.Person_Inf;

/**
 * Created by user on 16-11-7.
 */
public class PersonIdProvider extends ContentProvider {
    private static UriMatcher matcher=new UriMatcher(UriMatcher.NO_MATCH);
    private static final int PERSON_ID=1;   //查询person_inf所有Name
    private MyDatabaseHelper dbHelper;
    static {
        matcher.addURI(Person_Id.AYTHORITY,"person_id",PERSON_ID);
    }
    @Override
    public boolean onCreate() {
        dbHelper=new MyDatabaseHelper(this.getContext(),"Contacts.db3",1);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db=dbHelper.getReadableDatabase();
        switch (matcher.match(uri)){
            case PERSON_ID:
                return db.query("person_id",projection,selection,selectionArgs,null,null,sortOrder);
            default:
                throw new IllegalArgumentException("未知Uri:"+uri);
        }
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return "vnd.android.cursor.dir/com.moriarty.user.contacts.Person_Id";
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        long rowId=db.insert("person_id", Person_Id.person_Id._ID,values);
        if(rowId>0){
            Uri groupUri= ContentUris.withAppendedId(uri,rowId);
            getContext().getContentResolver().notifyChange(groupUri,null);
            return groupUri;
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db=dbHelper.getReadableDatabase();
        db.delete("person_id",selection,selectionArgs);
        getContext().getContentResolver().notifyChange(uri,null);
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        db.update("person_id",values,selection,selectionArgs);
        getContext().getContentResolver().notifyChange(uri,null);
        return 0;
    }
}
