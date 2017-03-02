package com.moriarty.user.contacts.Thread;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.moriarty.user.contacts.Activity.AddContacts;
import com.moriarty.user.contacts.Others.SignalManager;
import com.moriarty.user.contacts.R;
import com.moriarty.user.contacts.Service.QueryContactsService;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by user on 16-11-6.
 */
public class CheckMyInfoTask extends AsyncTask<Void,Void,Boolean> {
    SharedPreferences myInfo;
    Context mcontext;
    Handler handler;
    public CheckMyInfoTask(Context context, Handler handler){
        this.mcontext=context;
        myInfo=mcontext.getSharedPreferences("myself",mcontext.MODE_PRIVATE);  //获取toolbar上需要显示的个人信息
        this.handler=handler;
    }
    @Override
    protected Boolean doInBackground(Void... params) {
        HashMap<String,String> myInfoMap= QueryContactsService.getMyInfoToMap(myInfo);
        if(myInfoMap.get("phone")==null||myInfoMap.get("phone").equals(""))
            return false;
        else
            return true;
    }
    @Override
    protected void onPostExecute(Boolean has) {
        if (!has) {
            AlertDialog.Builder builder=new AlertDialog.Builder(mcontext)
                    .setIcon(mcontext.getResources().getDrawable(R.drawable.ic_dialog_alert_holo_light))
                    .setMessage(mcontext.getResources().getString(R.string.lack_of_myself_info))
                    .setTitle("注意");
            builder.setPositiveButton(mcontext.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Message message=new Message();
                    message.what= SignalManager.writeMyselfInfo_signal;
                    handler.sendMessage(message);
                }
            });
            builder.setNegativeButton(mcontext.getResources().getString(R.string.cancel),null).create().show();
            builder.setOnCancelListener(null);
        }
    }

    @Override
    protected void onPreExecute(){

    }
}
