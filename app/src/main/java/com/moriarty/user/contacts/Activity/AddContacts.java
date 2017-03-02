package com.moriarty.user.contacts.Activity;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.moriarty.user.contacts.Others.BroadcastManager;
import com.moriarty.user.contacts.Others.PopupMenuManager;
import com.moriarty.user.contacts.Others.ZoomBitmap;
import com.moriarty.user.contacts.Presenter.AddContactsPresenter;
import com.moriarty.user.contacts.Presenter.IAddContactsPresenter;
import com.moriarty.user.contacts.R;
import com.moriarty.user.contacts.Service.AddContactsService;
import com.moriarty.user.contacts.Service.GroupSettingService;
import com.moriarty.user.contacts.Service.QueryContactsService;
import com.moriarty.user.contacts.Service.SPDataManeger;

import java.util.ArrayList;

/**
 * Created by user on 16-8-5.
 */
public class AddContacts extends AppCompatActivity implements AddContactsView{
    public static EditText name;
    public static EditText phone;
    public static EditText email;
    public static EditText tiebaId;
    public static EditText weiboId;
    public static TextView groupType;
    private static final String currentTag="AddContacts";
    static String headPortrait="";
    String source_id;
    private ArrayList<String> allGroupName=new ArrayList<>();
    public Button selectGroupType,addGroupBtn;
    ImageButton add_imagebutton;
    AddContactsService addContactsService;
    AddContactsService.AddContactsBinder addContactsBinder;
    GroupSettingService groupSettingService;
    ImageView addContacts_Top;
    int flag=0;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    public static ArrayList<String> history=new ArrayList<>();
    ProgressDialog progressDialog;
    Context context;
    BroadcastManager broadcastManager=new BroadcastManager();
    SPDataManeger spDataManeger;
    PopupMenuManager popupMenuManager;
    IAddContactsPresenter addContactsPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_contact);
        context=this;
       // spDataManeger=new SPDataManeger(context);
       // popupMenuManager=new PopupMenuManager(context);
        addContactsPresenter=new AddContactsPresenter(context,this);

        name=(EditText)findViewById(R.id.name);
        phone=(EditText)findViewById(R.id.phone);
        email=(EditText)findViewById(R.id.email);
        groupType=(TextView)findViewById(R.id.groupType);
        tiebaId=(EditText)findViewById(R.id.addcontact_tieba);
        weiboId=(EditText)findViewById(R.id.addcontact_weibo);
        headPortrait="";
        addGroupBtn=(Button)findViewById(R.id.addgroup_btn);
        addGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View v0 = getLayoutInflater().inflate(R.layout.dialog_addgroup, null);
                addContactsPresenter.addGroupAction(v0);
                /*final EditText editText=(EditText)v0.findViewById(R.id.addgroup_edittext);
                new AlertDialog.Builder(v0.getContext()).setIcon(R.drawable.groupsetting_addgroup)
                        .setTitle(getString(R.string.new_group)).setView(v0)
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(!editText.getText().toString().equals("")){
                                    Intent addGroupIntent=new Intent();
                                    addGroupIntent.putExtra("groupsetting_flag",0);
                                    addGroupIntent.putExtra("newGroupName",editText.getText().toString());
                                    addGroupIntent.setAction("com.moriarty.service.GroupSettingService");
                                    addGroupIntent.setPackage(getPackageName());
                                    bindService(addGroupIntent, connection_add,BIND_AUTO_CREATE);
                                }
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).create().show();*/
            }
        });
        selectGroupType=(Button)findViewById(R.id.selectGroupType);
        selectGroupType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addContactsPresenter.selectGroupAction(selectGroupType,allGroupName);
               // allGroupName.clear();
               // popupMenuManager.showGroupMenu(selectGroupType,allGroupName);
            }
        });
        add_imagebutton=(ImageButton)findViewById(R.id.Add_ImageButton);
        add_imagebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addImage=new Intent();
                addImage.setAction(Intent.ACTION_PICK);   //通过这个action可以激活图库
                addImage.setType("image/*");   //设置要传递的数据类型
                startActivityForResult(addImage, 0);
            }
        });
        addContacts_Top=(ImageView)findViewById(R.id.AddContacts_Top);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        Intent intent=getIntent();  //如果是进行个人名片编辑，将sharedPreference中的信息写入各个控件中
        flag=intent.getIntExtra("flag",0);
        if(flag==1){
            ArrayList<String> recode;
            recode=intent.getStringArrayListExtra("mydata");
            tiebaId.setText(recode.get(4));
            weiboId.setText(recode.get(6));
            source_id=recode.get(8);  //记录我的source_id
            Invalidate(recode);
            history.clear();    //每次保存历史记录到集合中时先要将集合清空！这是静态变量，注意！！静态变量使用前都需要先清空。
            history.addAll(recode);  //保存修改前的历史数据
        }
        else if(flag==2){
            ArrayList<String> recode;
            recode=intent.getStringArrayListExtra("data");
            Invalidate(recode);
            groupType.setText(recode.get(4));
            tiebaId.setText(recode.get(5));
            weiboId.setText(recode.get(6));
            history.clear();
            history.addAll(recode);
        }
    }
    public void Invalidate(ArrayList<String> dataList){
        name.setText(dataList.get(0));
        phone.setText(dataList.get(1));
        email.setText(dataList.get(2));
        headPortrait=dataList.get(3);
        if(headPortrait.equals("")||headPortrait.equals("None"))  //如果为设置头像，即uri为空，addContacts_Top设置为默认头像
            addContacts_Top.setImageDrawable(getResources().getDrawable(R.drawable.contact_default2));
        else
            addContacts_Top.setImageBitmap(ZoomBitmap.getZoomBitmap(context,headPortrait,"large"));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            // 得到图片的全路径
            headPortrait=data.getData().toString();  //将URI转化为String
            Log.d(MainActivity.TAG,currentTag+headPortrait);
            // 通过路径加载图片
            //这里省去了图片缩放操作，如果图片过大，可能会导致内存泄漏
            this.addContacts_Top.setImageURI(Uri.parse(headPortrait));   //在将String转化为URI
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*private ServiceConnection connection_add=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            boolean returnValue;
            groupSettingService=((GroupSettingService.GroupSettingBinder)service).getService();
            returnValue=groupSettingService.addGroupIntoSQLite();
            unbindService(connection_add);
            if(returnValue){
                //Log.d("Moriarty","success");
                Toast.makeText(getApplication(),getString(R.string.addgroup_success),Toast.LENGTH_SHORT).show();
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };*/

    @Override
    public void unBindServiceConn(ServiceConnection connection) {

    }

    @Override
    public void showToast(String s) {
        Toast.makeText(this,s,Toast.LENGTH_SHORT).show();
    }

    public static class Card{
        public static String getName(){
            if(name.getText().toString()!=null&&!name.getText().toString().equals(""))
                return name.getText().toString();
            else{
                if(phone.getText().toString()!=null&&!phone.getText().toString().equals(""))
                    return phone.getText().toString();
                else
                    return email.getText().toString();
            }
        }
        public static String getPhone(){ return phone.getText().toString();}
        public static String getEmail() {return email.getText().toString();}
        public static String getGroup() {return groupType.getText().toString();}
        public static String getTiebaId() {return tiebaId.getText().toString();}
        public static String getTiebaUrl(){
            String id=tiebaId.getText().toString();
            if(id==null||id.equals("")||id.length()==0) {
                //Log.d("Moriarty", "AddContacts:" + "tiebaUrl is null");
                return "";
            }
            else {
                //Log.d("Moriarty", "AddContacts:" + "tiebaId is "+tiebaId.getText());
                return "http://tieba.baidu.com/home/main?un=" + tiebaId.getText().toString() + "&ie=utf-8&fr=pb";  //联系人的贴吧主页链接;
            }
        }
        public static String getWeiboId(){return weiboId.getText().toString();}
        public static String getWeiboUrl(){
            String id=weiboId.getText().toString();
            if(id==null||id.equals("")||id.length()==0)
                return "";
            else
                return "http://weibo.cn/u/"+weiboId.getText();
        }
        public static String getHeadPortrait(){return headPortrait;}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    /*private ServiceConnection conn=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            addContactsBinder=(AddContactsService.AddContactsBinder)service;
            addContactsService=(addContactsBinder).getService();
            Log.d(MainActivity.TAG,"I got service");
            if(flag==2){
                if(addContactsService.updateContactInContentProvider()&&addContactsService.updateContactInSQLite()){
                    if(history.get(1)!=null&&!history.get(1).equals(phone.getText().toString()))  //如果联系人的电话数据被修改，则其社交平台数据表的表明也要对应被修改
                        spDataManeger.changeSPData_TableName(history.get(1),phone.getText().toString());
                    unbindService(conn);
                    Log.d(MainActivity.TAG,"service is unbind");
                    broadcastManager.sendBroadCast_Sixth_WithValue(context,Card.getName());
                    broadcastManager.sendBroadcast(context,1);
                    progressDialog.dismiss();
                    AddContacts.this.finish();
                }
                else{     //这里需要进一步判断是哪一步未成功，需要继续进行联系人添加
                    Toast.makeText(AddContacts.this,getResources().getString(R.string.unabletoaddcontact),Toast.LENGTH_SHORT);
                    progressDialog.cancel();
                    unbindService(conn);
                }
            }
            else {    //flag==0
                if(addContactsService.addIntoContentProvide()&& addContactsService.addIntoSQLite()){
                    unbindService(conn);
                    Log.d(MainActivity.TAG,"service is unbind");
                    broadcastManager.sendBroadcast(context,1);
                    progressDialog.dismiss();
                    AddContacts.this.finish();
                }
                else{     //这里需要进一步判断是哪一步为成功，需要继续进行联系人添加
                    Toast.makeText(AddContacts.this,getResources().getString(R.string.unabletoaddcontact),Toast.LENGTH_SHORT);
                    progressDialog.cancel();
                    unbindService(conn);
                }
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    class AddTask extends AsyncTask<Void,Void,Boolean>{   //开启异步任务进行联系人数据的写入
        Context mContext;
        public AddTask(Context context){
            mContext=context;
            progressDialog=new ProgressDialog(mContext);
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            Intent startServiceIntent=new Intent();    //开启一个service,来执行添加联系人操作
            startServiceIntent.setAction("com.moriarty.service.ADDCONTACESSERVICE");
            startServiceIntent.setPackage(getPackageName());
            bindService(startServiceIntent,conn,BIND_AUTO_CREATE);
            Log.d(MainActivity.TAG,"service is connected");
            return true;
        }
        @Override
        protected void onPostExecute(Boolean result){

        }
        @Override
        protected void onPreExecute(){
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getResources().getString(R.string.wait_to_add));
            progressDialog.show();
        }
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int Id = item.getItemId();

        if (Id == R.id.action_settings) {
            switch (flag){
                case 0:
                    addContactsPresenter.confirmOthers();
                    break;
                case 1:
                    addContactsPresenter.confirmMyself(source_id);
                    break;
                case 2:
                    addContactsPresenter.confirmOthers();
                    break;
            }
            /*if(flag==0){   //新添加联系人信息
                AddTask addTask=new AddTask(this);
                Log.d(MainActivity.TAG,"data is prepare to add");
                addTask.execute();
                Log.d(MainActivity.TAG,"please wait");
            }
            else if(flag==1){   //保存自己的名片
                //writeInPreference();
                sharedPreferences=getSharedPreferences("myself",MODE_PRIVATE);
                editor=sharedPreferences.edit();
                //这里需要将该联系人的source_id传进去重新写入
                Log.d(MainActivity.TAG,currentTag+"source_id is"+source_id);
                boolean isSucceed=AddContactsService.writeInPreference(sharedPreferences,editor,context,source_id);
                if(isSucceed){
                    if(history.get(1)!=null&&!history.get(1).equals(phone.getText().toString()))  //如果自己的电话数据被修改，则其社交平台数据表的表明也要对应被修改
                        spDataManeger.changeSPData_TableName(history.get(1),phone.getText().toString());
                    Toast.makeText(this,getResources().getString(R.string.write_in_myself_succeed),Toast.LENGTH_SHORT).show();
                    broadcastManager.sendBroadcast(context,6);
                    broadcastManager.sendBroadcast(context,5);
                    AddContacts.this.finish();
                }
                else {
                    Toast.makeText(this,getResources().getString(R.string.write_in_myself_failed),Toast.LENGTH_SHORT).show();
                }
            }
            else if(flag==2){   //修改保存联系人信息
                AddTask addTask=new AddTask(this);
                addTask.execute();
            }*/
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void destroy(){

        //bug,无论是在此Acticity添加联系人还是添加分组，返回到主界面后都会数据为空。
        AddContacts.this.finish();
     //   onDestroy();
    }

    @Override
    public int getFlag() {
        return flag;
    }

    @Override
    public String getPhoneText() {
        return phone.getText().toString();
    }

    @Override
    public ArrayList<String> getHistory() {
        return history;
    }

    @Override
    public void onDestroy(){
        Log.d(MainActivity.TAG,currentTag+" Activity is destroy");
        super.onDestroy();
    }

}
