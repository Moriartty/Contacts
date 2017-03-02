package com.moriarty.user.contacts.Presenter;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.moriarty.user.contacts.Activity.AddContacts;
import com.moriarty.user.contacts.Activity.AddContactsView;
import com.moriarty.user.contacts.Activity.MainActivity;
import com.moriarty.user.contacts.Others.BroadcastManager;
import com.moriarty.user.contacts.Others.PopupMenuManager;
import com.moriarty.user.contacts.R;
import com.moriarty.user.contacts.Service.AddContactsService;
import com.moriarty.user.contacts.Service.GroupSettingService;
import com.moriarty.user.contacts.Service.SPDataManeger;

import java.util.ArrayList;

/**
 * Created by user on 17-2-25.
 */
public class AddContactsPresenter implements IAddContactsPresenter {
    Context context;
    GroupSettingService groupSettingService;
    AddContactsView addContactsView;
    PopupMenuManager popupMenuManager;
    AddContactsService addContactsService;
    AddContactsService.AddContactsBinder addContactsBinder;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    BroadcastManager broadcastManager=new BroadcastManager();
    SPDataManeger spDataManeger;
    ProgressDialog progressDialog;
    private String currentTag="AddContactsPresenter:";
    public AddContactsPresenter(Context context,AddContactsView addContactsView){
        this.context=context;
        this.addContactsView=addContactsView;
        popupMenuManager=new PopupMenuManager(context);
        spDataManeger=new SPDataManeger(context);
    }

    @Override
    public void addGroupAction(View v0) { //有bug,当在联系人添加界面新建分组后，退回到主界面后没有任何数据
        final EditText editText=(EditText)v0.findViewById(R.id.addgroup_edittext);
        new AlertDialog.Builder(v0.getContext()).setIcon(R.drawable.groupsetting_addgroup)
                .setTitle(context.getResources().getString(R.string.new_group)).setView(v0)
                .setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!editText.getText().toString().equals("")){
                            Intent addGroupIntent=new Intent();
                            addGroupIntent.putExtra("groupsetting_flag",0);
                            addGroupIntent.putExtra("newGroupName",editText.getText().toString());
                            addGroupIntent.setAction("com.moriarty.service.GroupSettingService");
                            addGroupIntent.setPackage(context.getPackageName());
                            context.bindService(addGroupIntent, connection_add,context.BIND_AUTO_CREATE);
                        }
                    }
                })
                .setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create().show();
    }

    @Override
    public void selectGroupAction(Button selectGroupType, ArrayList<String> allGroupName) {
        allGroupName.clear();  //每次点击都会开启查询，所以用完都要清空
        popupMenuManager.showGroupMenu(selectGroupType,allGroupName);
    }

    @Override
    public void confirmOthers() {
        AddTask addTask=new AddTask(context);
        addTask.execute();
    }

    @Override
    public void confirmMyself(String source_id) {
        sharedPreferences=context.getSharedPreferences("myself",context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
        //这里需要将该联系人的source_id传进去重新写入
        Log.d(MainActivity.TAG,currentTag+"source_id is"+source_id);
        boolean isSucceed= AddContactsService.writeInPreference(sharedPreferences,editor,context,source_id);
        if(isSucceed){
            ArrayList<String> history=addContactsView.getHistory();
            if(history.get(1)!=null&&!history.get(1).equals(addContactsView.getPhoneText()))  //如果自己的电话数据被修改，则其社交平台数据表的表明也要对应被修改
                spDataManeger.changeSPData_TableName(history.get(1),addContactsView.getPhoneText());
            addContactsView.showToast(context.getResources().getString(R.string.write_in_myself_succeed));
            broadcastManager.sendBroadcast(context,6);
            broadcastManager.sendBroadcast(context,5);
            addContactsView.destroy();
            //AddContact.this.finish();
        }
        else {
            addContactsView.showToast(context.getResources().getString(R.string.write_in_myself_failed));
        }
    }

    private ServiceConnection connection_add=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            boolean returnValue;
            groupSettingService=((GroupSettingService.GroupSettingBinder)service).getService();
            returnValue=groupSettingService.addGroupIntoSQLite();
            context.unbindService(connection_add);
            if(returnValue){
                addContactsView.showToast(context.getResources().getString(R.string.addgroup_success));
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private ServiceConnection conn=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            addContactsBinder=(AddContactsService.AddContactsBinder)service;
            addContactsService=(addContactsBinder).getService();
            Log.d(MainActivity.TAG,currentTag+"I got service");
            ArrayList<String> history=addContactsView.getHistory();
            if(addContactsView.getFlag()==2){
                if(addContactsService.updateContactInContentProvider()&&addContactsService.updateContactInSQLite()){
                    if(history.get(1)!=null&&!history.get(1).equals(addContactsView.getPhoneText()))  //如果联系人的电话数据被修改，则其社交平台数据表的表明也要对应被修改
                        spDataManeger.changeSPData_TableName(history.get(1),addContactsView.getPhoneText());
                    context.unbindService(conn);
                    Log.d(MainActivity.TAG,currentTag+"service is unbind");
                    broadcastManager.sendBroadCast_Sixth_WithValue(context, AddContacts.Card.getName());
                    broadcastManager.sendBroadcast(context,1);
                    progressDialog.dismiss();
                    addContactsView.destroy();
                    //AddContacts.this.finish();
                }
                else{     //这里需要进一步判断是哪一步未成功，需要继续进行联系人添加
                    addContactsView.showToast(context.getResources().getString(R.string.unabletoaddcontact));
                    progressDialog.cancel();
                    context.unbindService(conn);
                }
            }
            else {    //flag==0
                if(addContactsService.addIntoContentProvide()&& addContactsService.addIntoSQLite()){
                    context.unbindService(conn);
                    Log.d(MainActivity.TAG,"service is unbind");
                    broadcastManager.sendBroadcast(context,1);
                    progressDialog.dismiss();
                    addContactsView.destroy();
                    //AddContacts.this.finish();
                }
                else{     //这里需要进一步判断是哪一步为成功，需要继续进行联系人添加
                    addContactsView.showToast(context.getResources().getString(R.string.unabletoaddcontact));
                    progressDialog.cancel();
                    context.unbindService(conn);
                }
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    class AddTask extends AsyncTask<Void,Void,Boolean> {   //开启异步任务进行联系人数据的写入
        Context mContext;
        public AddTask(Context context){
            mContext=context;
            progressDialog=new ProgressDialog(mContext);
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            Intent startServiceIntent=new Intent();    //开启一个service,来执行添加联系人操作
            startServiceIntent.setAction("com.moriarty.service.ADDCONTACESSERVICE");
            startServiceIntent.setPackage(context.getPackageName());
            context.bindService(startServiceIntent,conn,context.BIND_AUTO_CREATE);
            Log.d(MainActivity.TAG,"service is connected");
            return true;
        }
        @Override
        protected void onPostExecute(Boolean result){

        }
        @Override
        protected void onPreExecute(){
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(context.getResources().getString(R.string.wait_to_add));
            progressDialog.show();
        }
    }
}
