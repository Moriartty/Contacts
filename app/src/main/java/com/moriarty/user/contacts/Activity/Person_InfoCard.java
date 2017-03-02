package com.moriarty.user.contacts.Activity;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.moriarty.user.contacts.Adapter.PagerAdapter_netInfo;
import com.moriarty.user.contacts.Dialog.ShareQRCode;
import com.moriarty.user.contacts.Fragment.Contacts_PersonInfo_QQZone;
import com.moriarty.user.contacts.Fragment.Contacts_PersonInfo_Tieba;
import com.moriarty.user.contacts.Fragment.Contacts_PersonInfo_Weibo;
import com.moriarty.user.contacts.Others.BroadcastManager;
import com.moriarty.user.contacts.Others.SignalManager;
import com.moriarty.user.contacts.Others.TwoDimentionCode;
import com.moriarty.user.contacts.Others.XmlToMap;
import com.moriarty.user.contacts.Others.ZoomBitmap;
import com.moriarty.user.contacts.Presenter.ILoadFragment;
import com.moriarty.user.contacts.Presenter.IQueryContacts;
import com.moriarty.user.contacts.Presenter.LoadNetDataFragment;
import com.moriarty.user.contacts.Presenter.QueryContacts;
import com.moriarty.user.contacts.R;
import com.moriarty.user.contacts.Service.AddContactsService;
import com.moriarty.user.contacts.Service.QueryContactsService;
import com.moriarty.user.contacts.Service.SPDataManeger;
import com.moriarty.user.contacts.Thread.ClientThread;

import org.dom4j.DocumentException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 16-9-24.
 */
public class Person_InfoCard extends AppCompatActivity implements PersonInfoCardView{
    private final static String currentTag="Person_InfCard:";
    String name="";
    ImageView person_info_headrait,shareTDC_img,messageRecode_img,collect_img,TDC_Image,dialog_headrait;
    TextView dialog_card_name,person_info_email,person_info_phone,collect_text;
    String[] detail; //记录phonenumber,email,tiebaId,tiebaUrl
    String uri;
    String tiebaId,tiebaUrl;
    String weiboId,weiboUrl;
    BroadcastReceiver receiver6;
    Toolbar toolbar;
    CollapsingToolbarLayout mToolbarLayout;
    private ViewPager mViewPager;
    private PagerAdapter_netInfo mPagerAdapter;
    Button[] mBtnTabs=new Button[3];
    String[] addresses;
    SwipeRefreshLayout swipeRefreshLayout;
    private int flag;
    ArrayList<String> recode;
    SharedPreferences myInfo;
    ClientThread clientThread;
    Handler handler;
    int currentPage=0;
    Context context;
    String phoneText;
    BroadcastManager broadcastManager=new BroadcastManager();
    ContentResolver contentResolver;
    public static Handler handler2;
    SPDataManeger spDataManeger;
    ILoadFragment loadNetDataFragment;
    IQueryContacts queryContacts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.person_info);
        context=this;
        contentResolver=getContentResolver();
        spDataManeger=new SPDataManeger(context);

        handler=new Handler(){
            @Override
            public void handleMessage(Message message){
                if(message.what== SignalManager.return_SPData_signal){
                   // Log.d("Moriarty","Person_InfoCard:"+message.obj.toString());
                    loadNetDataFragment.receiveMessage(message,tiebaId,phoneText);
                }
            }
        };

        loadNetDataFragment=new LoadNetDataFragment(context,Person_InfoCard.this);
        loadNetDataFragment.initializeThread(handler);
        queryContacts=new QueryContacts();


        handler2=new Handler(){  //用于接受fragment加载完毕的返回信息
            @Override
            public void handleMessage(Message message){
                loadNetDataFragment.initializeFragment(message,phoneText,tiebaId);
            }
        };

        person_info_headrait=(ImageView)findViewById(R.id.person_info_headrait);
        shareTDC_img=(ImageView)findViewById(R.id.shareTDC_img);
        messageRecode_img=(ImageView)findViewById(R.id.messageRecode_img);
        collect_img=(ImageView)findViewById(R.id.collect_img);
        person_info_phone=(TextView)findViewById(R.id.person_info_phone);
        person_info_email=(TextView)findViewById(R.id.person_info_email);
        collect_text=(TextView)findViewById(R.id.collect_text);

        final Intent intent=getIntent();
        flag=intent.getIntExtra("flag",2);
        if(flag==2){   //显示联系人信息
            name=intent.getStringExtra("info");
            invalidate();
            Log.d(MainActivity.TAG,currentTag+name);
        }
        else if(flag==1){   //显示个人信息
            recode=intent.getStringArrayListExtra("mydata");
            Log.d(MainActivity.TAG,currentTag+"my source_id is recode"+recode.get(8));
            invalidate();
        }

        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.person_info_refresh);
        swipeRefreshLayout.setProgressViewOffset(true,50,200);
        swipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE);
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.dodgerblue));
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.white));
        swipeRefreshLayout.setEnabled(false);   //禁止下拉刷新

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.refresh_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (currentPage){
                    case 0:loadNetDataFragment.sendTiebaRefreshOrder(tiebaUrl,phoneText,name);break;
                    case 1:loadNetDataFragment.sendWeiboRefreshOrder(weiboUrl,phoneText,name);break;
                }
            }
        });

        toolbar = (Toolbar) findViewById(R.id.toolbar_person_info);
        toolbar.setTitle(name);
        setSupportActionBar(toolbar);

        IntentFilter intentFilter=new IntentFilter("BC_SIX");    //动态广播
        receiver6=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (flag){
                    case 1:
                        myInfo=getSharedPreferences("myself",MODE_PRIVATE);
                        recode=QueryContactsService.getMyInformation(myInfo);
                        break;
                    case 2:
                        name=intent.getStringExtra("Name");
                        break;
                }
                invalidate();
                loadNetDataFragment.sendTiebaRefreshOrder(tiebaUrl,phoneText,name);
                loadNetDataFragment.sendWeiboRefreshOrder(weiboUrl,phoneText,name);
            }
        };
        registerReceiver(receiver6,intentFilter);

        shareTDC_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareQRCode.showQRCode(context,name,uri);
            }
        });

        collect_img.setOnClickListener(collect_img_onClickListener);

        addresses=new String[]{getResources().getString(R.string.addresses_tieba),getResources().getString(R.string.addresses_weibo),
                        getResources().getString(R.string.address_qqzone)};

        mBtnTabs[0]=(Button)findViewById(R.id.person_info_fragment_button_tieba);
        mBtnTabs[0].setBackgroundColor(getResources().getColor(R.color.white));
        mBtnTabs[0].setTextColor(getResources().getColor(R.color.colorPrimary));
        mBtnTabs[0].setText(addresses[0]);
        mBtnTabs[1]=(Button)findViewById(R.id.person_info_fragment_button_weibo);
        mBtnTabs[1].setBackgroundColor(getResources().getColor(R.color.white));
        mBtnTabs[1].setText(addresses[1]);
        mBtnTabs[2]=(Button)findViewById(R.id.person_info_fragment_button_qqzone);
        mBtnTabs[2].setBackgroundColor(getResources().getColor(R.color.white));
        mBtnTabs[2].setText(addresses[2]);
        mBtnTabs[0].setOnClickListener(mTabClickListener);
        mBtnTabs[1].setOnClickListener(mTabClickListener);
        mBtnTabs[2].setOnClickListener(mTabClickListener);
        mViewPager = (ViewPager) findViewById(R.id.viewPager2);
        mPagerAdapter=new com.moriarty.user.contacts.Adapter.PagerAdapter_netInfo(getSupportFragmentManager(),phoneText);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOnPageChangeListener(mPageChangeListener);
        mViewPager.setCurrentItem(0);
    }

    public View.OnClickListener collect_img_onClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(flag==2){
                if(QueryContactsService.IsCollected(name)){
                    showToast(getResources().getString(R.string.collect_cancle));
                    collect_text.setText(getResources().getString(R.string.person_info_imgbtn_third));
                    AddContactsService.update_CollectedInfo(name,0,contentResolver);
                }
                else {
                    showToast(getResources().getString(R.string.collect_succeed));
                    collect_text.setText(getResources().getString(R.string.cancle_collect));
                    AddContactsService.update_CollectedInfo(name,1,contentResolver);
                }
                broadcastManager.sendBroadcast(context,1);
            }
            else {//不允许收藏自己
                showToast(getResources().getString(R.string.cannot_to_collect_myself));
            }
        }
    };

    @Override
    public void onResume(){
        super.onResume();
        mToolbarLayout=(CollapsingToolbarLayout)findViewById(R.id.toolbar_layout);
        mToolbarLayout.setTitle(name);
    }

    public void invalidate(){
       // Log.d("Moriarty","Person_InfoCard:"+name);
        switch (flag){
            case 1:     //本人信息
                name=recode.get(0);
                //如果没有联系人的电话或邮箱信息，则文字提示用户
                person_info_phone.setText(recode.get(1)==null||recode.get(1).equals("")?getResources().getString(R.string.no_phone_details):recode.get(1));
                person_info_email.setText(recode.get(2)==null||recode.get(2).equals("")?getResources().getString(R.string.no_email_details):recode.get(2));
                uri=recode.get(3);
                tiebaId=recode.get(4);
                tiebaUrl=recode.get(5);
                weiboId=recode.get(6);
                weiboUrl=recode.get(7);
                break;
            case 2:   //联系人信息
                uri= QueryContactsService.getHeadPicPath_Person(name);
                Log.d(MainActivity.TAG,currentTag+uri);
                try{
                    detail=queryContacts.queryContact(name,context);
                }catch (Exception e){
                    e.printStackTrace();
                }
                person_info_phone.setText(detail[0]==null||detail[0].length()==0?getResources().getString(R.string.no_phone_details):detail[0]);
                person_info_email.setText(detail[1]==null||detail[1].length()==0?getResources().getString(R.string.no_email_details):detail[1]);
                tiebaId=detail[2];
                tiebaUrl=detail[3];
                weiboId=detail[4];
                weiboUrl=detail[5];
                break;
        }
        phoneText=person_info_phone.getText().toString();
        if(uri.equals("None")||uri.equals("")){
            this.person_info_headrait.setImageDrawable(getResources().getDrawable(R.drawable.contact_default2));
        }
        else
            this.person_info_headrait.setImageBitmap(ZoomBitmap.getZoomBitmap(context,uri,"medium"));
        if(QueryContactsService.IsCollected(name))
            collect_text.setText(getResources().getString(R.string.cancle_collect));
        else
            collect_text.setText(getResources().getString(R.string.person_info_imgbtn_third));

    }

    @Override
    public void onDestroy(){
        unregisterReceiver(receiver6);
        super.onDestroy();
    }

    private void changeTabColor(int flag){
        for(int i=0;i<3;i++){
            if(i==flag)
                mBtnTabs[i].setTextColor(getResources().getColor(R.color.blue));
            else
                mBtnTabs[i].setTextColor(getResources().getColor(R.color.black));
        }
    }

    private View.OnClickListener mTabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            if (v == mBtnTabs[0])
            {
                mViewPager.setCurrentItem(0);
                changeTabColor(0);
            } else if (v == mBtnTabs[1])
            {
                mViewPager.setCurrentItem(1);
                changeTabColor(1);
            }
            else if(v == mBtnTabs[2]){
                mViewPager.setCurrentItem(2);
                changeTabColor(2);
            }
        }
    };

    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int arg0)
        {
            if(arg0==1){
                changeTabColor(1);
            }
            else if(arg0==0){
                changeTabColor(0);
            }
            else{
                changeTabColor(2);
            }
            Log.d(MainActivity.TAG,currentTag+"currentpage is "+arg0);
            currentPage=arg0;
        }
        @Override
        public void onPageScrolled(int arg0, float arg1,int arg2)
        {
        }
        @Override
        public void onPageScrollStateChanged(int arg0)
        {
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_edit) {
            Intent editIntent=new Intent(Person_InfoCard.this,AddContacts.class);
            ArrayList<String> dataList=new ArrayList<>();
            switch (flag){
                case 1:    //准备修改个人信息
                    dataList=recode;
                    editIntent.putExtra("flag",1);
                    editIntent.putExtra("mydata",dataList);
                    Log.d(MainActivity.TAG,currentTag+"my source_id is "+dataList.get(8));
                    break;
                case 2:    //准备修改联系人信息
                    dataList.add(name);   //姓名
                    dataList.add(detail[0]);   //电话
                    dataList.add(detail[1]);   //email
                    dataList.add(uri);       //头像uri
                    dataList.add(QueryContactsService.getPosition_Person(name));
                    dataList.add(detail[2]);  //tiebaId
                    dataList.add(detail[4]);   //weiboId
                    editIntent.putExtra("flag",2);
                    editIntent.putExtra("data",dataList);
                    break;
            }
            startActivity(editIntent);
            return true;
        }
        else if(id==R.id.change_personinfo_bg){
            //待补充
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setSRLayoutFalse() {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void setSRLayoutTrue() {
        swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void showToast(String toast) {
        Toast.makeText(this,toast,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void getFromNet(int flag) {
        switch (flag){
            case SignalManager.tiebaFragment_Prepared:  //tiebaFragment加载完毕
                loadNetDataFragment.sendTiebaRefreshOrder(tiebaUrl,phoneText,name);
                break;
            case SignalManager.weiboFragment_Prepared:
                loadNetDataFragment.sendWeiboRefreshOrder(weiboUrl,phoneText,name);
                break;
        }
    }
}
