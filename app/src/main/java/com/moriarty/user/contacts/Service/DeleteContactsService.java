package com.moriarty.user.contacts.Service;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.moriarty.user.contacts.Activity.MainActivity;
import com.moriarty.user.contacts.Database.ToolClass.Person_Id;
import com.moriarty.user.contacts.Database.ToolClass.Person_Inf;
import com.moriarty.user.contacts.Others.BroadcastManager;
import com.moriarty.user.contacts.R;

/**
 * Created by user on 16-8-11.
 */
public class DeleteContactsService extends Service {
    private String name;
    ContentResolver contentResolver;
    SPDataManeger spDataManeger;
    public class DeleteContactsBinder extends Binder{
        public DeleteContactsService getService(){
            return DeleteContactsService.this;
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        contentResolver=getContentResolver();
        name=intent.getStringExtra("name");
        spDataManeger=new SPDataManeger(this);
        return new DeleteContactsBinder();
    }
    @Override
    public void onCreate(){
        super.onCreate();
    }

    public boolean deleteContacts(){
        //Log.d("name:","is"+name);
        //先查询到name对应的rawcontactsid
        ContentResolver cr = getContentResolver();
        int re = cr.delete(ContactsContract.RawContacts.CONTENT_URI,
                "display_name = ?",
                new String[]{name}
        );
        if (re > 0) {
            Toast.makeText(DeleteContactsService.this, getString(R.string.deletecontact_success), Toast.LENGTH_SHORT).show();
            new BroadcastManager().sendBroadcast(this,1);
            return true;
        }
        else
            return false;
    }

    public boolean deleteContactsInSQLite(){
        try{
            contentResolver.delete(Person_Inf.person_Inf.PERSON_INF_URI,"Name=?",new String[]{name});
            String tel=QueryContactsService.queryTel(name,this);
            contentResolver.delete(Person_Id.person_Id.PERSON_ID_URI,"Tel=?",new String[]{tel});
            deleteSPData(tel);   //删除联系人的同时需要把其社交动态数据删除
            return true;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }
    public void deleteSPData(String tel){
        String tiebaTableName="Contacts_"+tel+"_Tieba";
        String weiboTableName="Contacts_"+tel+"_Weibo";
        spDataManeger.deleteTiebaData(tiebaTableName);
        spDataManeger.deleteWeiboData(weiboTableName);
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
    }
}
