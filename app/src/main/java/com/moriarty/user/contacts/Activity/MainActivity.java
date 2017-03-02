package com.moriarty.user.contacts.Activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jauker.widget.BadgeView;
import com.moriarty.user.contacts.Dialog.ShareQRCode;
import com.moriarty.user.contacts.Others.BroadcastManager;
import com.moriarty.user.contacts.Others.NetworkManager;
import com.moriarty.user.contacts.Others.SignalManager;
import com.moriarty.user.contacts.Others.TwoDimentionCode;
import com.moriarty.user.contacts.Others.XmlToMap;
import com.moriarty.user.contacts.Others.ZoomBitmap;
import com.moriarty.user.contacts.Presenter.IMainPresenter;
import com.moriarty.user.contacts.Presenter.MainActivityPresenter;
import com.moriarty.user.contacts.R;
import com.moriarty.user.contacts.Service.AddContactsService;
import com.moriarty.user.contacts.Service.QueryContactsService;
import com.moriarty.user.contacts.Thread.CheckMyInfoTask;
import com.moriarty.user.contacts.Thread.ReserveTask;

import org.dom4j.DocumentException;



public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,MainView {

    public static final String TAG ="Moriarty" ;
    private static final String currentTag="MainActivity";
    private ViewPager mViewPager;       //实现滑动的组件
    private PagerAdapter mPagerAdapter;    //viewPager适配器
    public static String[] addresses;      //包含两个fragment的命名
    private Button[] mBtnTabs=new Button[3];
    private int currentTab=0;               //记录当前的页面编号
    public static ArrayList<String> names = new ArrayList<String>();   //存储联系人姓名
    public static ArrayList<ArrayList<String>> details=new ArrayList<ArrayList<String>>();//存储联系人其他信息
    private QueryContactsService queryContactsService;     //查询服务对象
    public static ArrayList<String> groupName=new ArrayList<>(); //记录所有小组名
    public static ArrayList<ArrayList<String>> groupContent=new ArrayList<>();  //记录每个小组的每个成员
    public static HashMap<String,String> headPortraits=new HashMap<>();  //记录每个人的头像
    public static ArrayList<String> collectedInfo=new ArrayList<>();
    BroadcastReceiver receiver,receiver2,receiver5;
    int REQUEST_PERMISSION1=110;
    int REQUEST_PERMISSION2=111;
    SharedPreferences myInfo;
    SharedPreferences.Editor editor;
    ImageView toolbar_my_headrait,nav_my_headrait,nav_my_qrcode;
    TextView toolbar_my_name,nav_my_name,nav_my_phone;
    ArrayList<String> myInfoList;
    DrawerLayout drawer;
    Toolbar toolbar;
    Context context;
    public static String networkAddress="192.168.43.222";
    public static int networkPort=10000;
    public static String overTag="[over]"+"\r\n";
    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    public Handler handler;
    public Handler handler2;
    IMainPresenter mainPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        context=this;
        mainPresenter=new MainActivityPresenter(context,MainActivity.this);
        handler=new Handler(){
            @Override
            public void handleMessage(Message message){
                if(message.what== SignalManager.writeMyselfInfo_signal){
                    mainPresenter.skip2AddContactsView(myInfoList);
                }
            }
        };

        handler2=new Handler(){
            @Override
            public void handleMessage(Message message){
                mainPresenter.handleReturnData(message);
            }
        };
        //检查权限
        mainPresenter.inspectPermission(handler);

        toolbar_my_name=(TextView)findViewById(R.id.toolbar_my_name);
        toolbar_my_headrait=(ImageView)findViewById(R.id.toolbar_my_headrait);
        nav_my_phone=(TextView)findViewById(R.id.nav_my_phone);
        nav_my_name=(TextView)findViewById(R.id.nav_my_name);
        nav_my_headrait=(ImageView)findViewById(R.id.nav_my_headrait);
        nav_my_qrcode=(ImageView)findViewById(R.id.nav_my_qrcode);
        nav_my_qrcode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.closeDrawer(GravityCompat.START);  //点击图标，显示我的二维码
                ShareQRCode.showQRCode(context,"",myInfoList.get(3));
            }
        });

        toolbar_my_headrait.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(GravityCompat.START);
            }
        });

        addresses=new String[]{getString(R.string.allcontacts),getString(R.string.mygroup),getString(R.string.system_valuesort)};

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");  //将toolbar上的title先隐藏
        setSupportActionBar(toolbar);

        FloatingActionButton add_contact = (FloatingActionButton) findViewById(R.id.fab);
        add_contact.setOnClickListener(add_contact_onClickListener);

        FloatingActionButton voice_search = (FloatingActionButton) findViewById(R.id.voice_search);
        voice_search.setOnClickListener(voice_search_onClickListener);

        IntentFilter intentFilter=new IntentFilter("BC_ONE");    //动态广播,更新全局界面，先更新数据，然后会在service中发送二号广播进行fragment的更新
        receiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                currentTab=mViewPager.getCurrentItem();
                //Log.d(TAG,"MainActivity:"+"currentpage is "+currentTab);
                invalidate();
            }
        };
        registerReceiver(receiver,intentFilter);

        IntentFilter intentFilter2=new IntentFilter("BC_TWO");    //动态广播，数据已经准备好了，准备更新fragment
        receiver2=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG,currentTag+" names size is "+names.size());
                Log.d(TAG,"Fragment is prepare to refresh");
                mPagerAdapter=new com.moriarty.user.contacts.Adapter.PagerAdapter(getSupportFragmentManager());
                mViewPager.setAdapter(mPagerAdapter);
                mViewPager.setOnPageChangeListener(mPageChangeListener);
                setCurrentItem(currentTab);//更新完fragment后，利用之前保存的currentTab回到初始操作页面
            }
        };
        registerReceiver(receiver2,intentFilter2);

        IntentFilter intentFilter5=new IntentFilter("BC_FIVE");    //动态广播,准备更新Toolbar和Navigation
        receiver5=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
               invalidateToolbarAndNav();
            }
        };
        registerReceiver(receiver5,intentFilter5);

        mBtnTabs[0]=(Button)findViewById(R.id.i1);
        mBtnTabs[0].setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        mBtnTabs[0].setText(addresses[0]);
        mBtnTabs[1]=(Button)findViewById(R.id.i2);
        mBtnTabs[1].setBackgroundColor(getResources().getColor(R.color.white));
        mBtnTabs[1].setText(addresses[1]);
        mBtnTabs[2]=(Button)findViewById(R.id.i3);
        mBtnTabs[2].setBackgroundColor(getResources().getColor(R.color.white));
        mBtnTabs[2].setText(addresses[2]);
        mBtnTabs[0].setOnClickListener(mTabClickListener);
        mBtnTabs[1].setOnClickListener(mTabClickListener);
        mBtnTabs[2].setOnClickListener(mTabClickListener);
        mViewPager = (ViewPager) findViewById(R.id.viewPager1);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(     //actionbarDrawerToggle暂时不使用！
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        Drawable drawable=(Drawable)toolbar_my_headrait.getDrawable();
        toolbar.setNavigationIcon(drawable);   //将toolbar上的图标设置为空，以自己头像取而代之
        //无法在.xml文件中直接设置icon,只能在这个地方设置！！！
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View view=findViewById(R.id.perfect);
        BadgeView badgeView=new BadgeView(context);
        badgeView.setTargetView(view);
        badgeView.setBadgeGravity(Gravity.TOP|Gravity.RIGHT);
        badgeView.setVisibility(View.VISIBLE);

    }
    private OnClickListener voice_search_onClickListener=new OnClickListener() {
        @Override
        public void onClick(View v) {
            try{
                //通过Intent传递语音识别的模式，开启语音
                Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                //语言模式和自由模式的语音识别
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                //提示语音开始
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "开始语音");
                //开始语音识别
                startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
            }catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
                showToast("找不到语音设备");
            }
        }
    };
    private OnClickListener add_contact_onClickListener=new OnClickListener() {
        @Override
        public void onClick(View v) {
            currentTab=mViewPager.getCurrentItem();  //每次只是更新fragment,MainActivity中的currentTab不改变
            Intent intent=new Intent(MainActivity.this,AddContacts.class);
            intent.putExtra("flag",0);
            startActivity(intent);
        }
    };
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        //回调获取从谷歌得到的数据
        mainPresenter.voiceSearch(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==REQUEST_PERMISSION2&&grantResults[0]== PackageManager.PERMISSION_GRANTED){
            mainPresenter.checkMySelfInfo(handler);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onResume(){
        super.onResume();
    }
    @Override
    public void onRestart(){
        super.onRestart();
        Log.d(TAG,"MainActivity:"+"restart");
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(receiver);   //当进程结束时，取消对三个广播的注册
        unregisterReceiver(receiver2);
        unregisterReceiver(receiver5);
    }

    @Override
    public void invalidate(){
        //初始化接收数据时，先清空所有存数据的集合变量
        names.clear();
        details.clear();
        groupContent.clear();
        groupName.clear();
        headPortraits.clear();
        collectedInfo.clear();

        Intent queryIntent=new Intent();
        queryIntent.setAction("com.moriarty.service.QUERYCONTACTSSERVICE");
        queryIntent.setPackage(getPackageName());
        bindService(queryIntent,connection,BIND_AUTO_CREATE);
    }

    @Override
    public void enduePermission(String[] permissions) {
        if(ContextCompat.checkSelfPermission(context,permissions[0])!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{permissions[0]},REQUEST_PERMISSION1);
        }
        if(ContextCompat.checkSelfPermission(context,permissions[1])!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{permissions[1]},REQUEST_PERMISSION2);
        }
    }

    @Override
    public void setCurrentItem(int flag) {
        mViewPager.setCurrentItem(flag);
    }

    private void invalidateToolbarAndNav(){
        myInfo=getSharedPreferences("myself",MODE_PRIVATE);  //获取toolbar上需要显示的个人信息
        myInfoList=QueryContactsService.getMyInformation(myInfo);//从sharedPreference中获取本人信息
        setToolbar();        //设置toolbar上的本人信息
        setNavigation();     //设置navigation上的本人信息
    }

    private void setToolbar(){
        if(!hasHeadrait(myInfoList.get(3)))
            toolbar_my_headrait.setImageDrawable(getResources().getDrawable(R.drawable.contact_default2));
        else{

            toolbar_my_headrait.setImageBitmap(ZoomBitmap.getZoomBitmap(context,myInfoList.get(3),"small"));
        }
        toolbar_my_name.setText(myInfoList.get(0));
    }
    private void setNavigation(){
        if(!hasHeadrait(myInfoList.get(3)))
            nav_my_headrait.setImageDrawable(getResources().getDrawable(R.drawable.contact_default2));
        else
            nav_my_headrait.setImageBitmap(ZoomBitmap.getZoomBitmap(context,myInfoList.get(3),"medium"));
        nav_my_name.setText(myInfoList.get(0));
        nav_my_phone.setText(myInfoList.get(1));
    }
    private boolean hasHeadrait(String headRaitRecode){
        if(headRaitRecode.equals("")||headRaitRecode==null||headRaitRecode.equals("None"))
            return false;
        else
            return true;
    }

    private ServiceConnection connection=new ServiceConnection() {    //查询服务的连接对象
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            queryContactsService=((QueryContactsService.QueryContactsBinder)service).getService();
            invalidateToolbarAndNav();   //更新toolbar和Drawerlayout上的信息
            names.addAll(queryContactsService.queryContactsName());
            Log.d(TAG,currentTag+names.size());
            getGroupName(queryContactsService.queryForGroup());    //在查询服务中生成了包含所有信息的Map
            headPortraits.putAll(queryContactsService.queryForHeadPortrait());  //获取每个人对应头像的URI
            collectedInfo.addAll(queryContactsService.queryForCollectedInfo());
            //然后开始异步加载和缓存头像资源
            Log.d(TAG,"Data is prepared");
            unbindService(connection);         // 在下次连接服务之前必须先断开此次服务
            Log.d(TAG,"QueryContactsService is unBinded");
            QueryContactsService.names.clear();     //每次查询完数据库要清空数据库静态集合变量！！！
            QueryContactsService.details.clear();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    public Boolean getGroupName(HashMap<String,ArrayList<String>> groupMap){     //person_inf中的group，不含重复,但可能不完全
        Iterator<String> groupIt=groupMap.keySet().iterator();
        while((groupIt.hasNext())){
            String tempGroupName=groupIt.next();
            groupName.add(tempGroupName);  //获取Map中的GroupName
            groupContent.add(groupMap.get(tempGroupName));  //获取每个Group中的people
        }
        return true;
    }

    private void changeTabBtnColor(int flag){
        for(int i=0;i<3;i++){
            if(flag==i)
                mBtnTabs[i].setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            else
                mBtnTabs[i].setBackgroundColor(getResources().getColor(R.color.white));
        }
    }

    private OnClickListener mTabClickListener = new OnClickListener() {
        @Override
        public void onClick(View v)
        {
            if (v == mBtnTabs[0])
            {
                setCurrentItem(0);
                changeTabBtnColor(0);
            } else if (v == mBtnTabs[1])
            {
                setCurrentItem(1);
                changeTabBtnColor(1);
            }
            else{
                setCurrentItem(2);
                changeTabBtnColor(2);
            }
        }
    };

    private OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageSelected(int arg0)
        {
            //mTabWidget.setCurrentTab(arg0);
            if(arg0==1){
                changeTabBtnColor(1);
            }
            else if(arg0==0){
                changeTabBtnColor(0);
            }
            else if(arg0==2){
                changeTabBtnColor(2);
            }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_scan) {
            //Toast.makeText(this,"扫描二维码",Toast.LENGTH_SHORT).show();
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.mediatek.camera");
            intent.setAction("com.tct.camera.STARTQRSCAN");  //开启tcl手机的二维码扫描程序
            startActivity(intent);
            return true;
        }
        else if(id == R.id.action_reserve){
            if(!NetworkManager.isConnectionAvailable(context)){  //先检查客户端网络情况
                ReserveTask reserveTask=new ReserveTask(context,0,handler2);
                reserveTask.execute();
            }
            else
                Toast.makeText(context,getResources().getString(R.string.network_inaccessible),Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        // Handle navigation view item clicks here.
        drawer.closeDrawer(GravityCompat.START);
        drawer.setDrawerListener(new DrawerLayout.DrawerListener() {
            int id=item.getItemId();
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (id == R.id.nav_my_info_card) {
                    //这里需要重新获取sharePreference文件中的数据
                    myInfo=getSharedPreferences("myself",MODE_PRIVATE);  //获取toolbar上需要显示的个人信息
                    ArrayList<String> myNewInfoList=QueryContactsService.getMyInformation(myInfo);//从sharedPreference中获取本人信息
                    if(myNewInfoList.get(1)!=null){
                        mainPresenter.skip2PersonInfo(myNewInfoList);
                    }
                    else{
                        mainPresenter.skip2AddContactsView(myNewInfoList);
                    }
                } else if (id == R.id.nav_gallery) {
                    Toast.makeText(MainActivity.this,"主题设置",Toast.LENGTH_SHORT).show();

                } else if (id == R.id.nav_manage) {

                } else if (id == R.id.nav_share) {
                    TwoDimentionCode twoDimentionCode=new TwoDimentionCode();
                    Bitmap bitmap=twoDimentionCode.generateTDC("",null);
                    twoDimentionCode.shareImage(bitmap,context);

                } else if (id == R.id.nav_send) {
                  /*  if(!NetworkManager.isConnectionAvailable(context)){
                        ReserveTask reserveTask=new ReserveTask(context,1);
                        reserveTask.execute();
                    }
                    else
                        Toast.makeText(context,getResources().getString(R.string.network_inaccessible),Toast.LENGTH_SHORT).show();*/
                }
                item.setChecked(false);
                id=0;
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        return true;
    }

    @Override
    public void showToast(String s) {
        Toast.makeText(context,s,Toast.LENGTH_SHORT).show();
    }


}
