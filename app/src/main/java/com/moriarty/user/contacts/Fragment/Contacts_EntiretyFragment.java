package com.moriarty.user.contacts.Fragment;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moriarty.user.contacts.Activity.MainActivity;
import com.moriarty.user.contacts.Activity.Person_InfoCard;
import com.moriarty.user.contacts.Adapter.Entirety_ContactsListAdapter;
import com.moriarty.user.contacts.Others.HandleContact;
import com.moriarty.user.contacts.SearchContact.CharacterParser;
import com.moriarty.user.contacts.SearchContact.ClearEditText;
import com.moriarty.user.contacts.SearchContact.PinyinComparator;
import com.moriarty.user.contacts.R;
import com.moriarty.user.contacts.SearchContact.SideBar;
import com.moriarty.user.contacts.SearchContact.SortModel;
import com.moriarty.user.contacts.Service.DeleteContactsService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by user on 16-8-5.
 */
public class Contacts_EntiretyFragment extends Fragment{
    private static final String currentTag="Contacts_EntiretyFragment:";
    private RecyclerView mRecyclerView;
    Entirety_ContactsListAdapter mAdapter;
    private SideBar sideBar;
    private TextView dialog;
    private ClearEditText mClearEditText;
    private CharacterParser characterParser;
    private List<SortModel> SourceDateList;
    private PinyinComparator pinyinComparator;
    View v;
    Context context;
    BroadcastReceiver receiver,receiver2;
    public static ArrayList<String> names = new ArrayList<String>();
    public static HashMap<String,String> headPortraits=new HashMap<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        context=getActivity();
        names.clear();
        headPortraits.clear();
        names.addAll(MainActivity.names);
        headPortraits.putAll(MainActivity.headPortraits);


        IntentFilter intentFilter=new IntentFilter("BC_FOUR");    //动态广播,接受来自GroupSettingService的广播
        receiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(HandleContact.alertDialog!=null)    //HandleContact.alertDialog的广播监听器任布置在两个fragment中，都在onDestroy()中取消监听。
                    HandleContact.alertDialog.dismiss();//关闭AlertDialog
                else
                    Log.d(MainActivity.TAG,currentTag+"alertDialog is null");
            }
        };
        context.registerReceiver(receiver,intentFilter);

        IntentFilter intentFilter1=new IntentFilter("BC_SEVEN");  //接收语音识别结果并进行搜索
        receiver2=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                /*
                对语音搜索的结果进行遍历，找出匹配程度最大的target。
                */
                ArrayList<String> resultData;
                resultData=intent.getStringArrayListExtra("Result");
                //Log.d(MainActivity.TAG,currentTag+name);
                if(resultData!=null&&resultData.size()>0){
                    int size=resultData.size();
                    String target=resultData.get(0);
                    int returnNum=filterData(target).size();
                    for(int i=1;i<size;i++){
                        if(filterData(resultData.get(i)).size()>returnNum){
                            target=resultData.get(i);
                        }
                    }
                    mClearEditText.setText(target);
                }
            }
        };
        context.registerReceiver(receiver2,intentFilter1);

        v = inflater.inflate(R.layout.fragment_viewpager2_layout1, null);
        Log.d(MainActivity.TAG,"EntiretyFragment is prepare to initView");
        initViews();
        return v;
    }
    @Override
    public void onDestroy(){
        Log.d(MainActivity.TAG,currentTag+" Entirety is destroy");
        getContext().unregisterReceiver(receiver);
        getContext().unregisterReceiver(receiver2);
        super.onDestroy();
    }

    private void initViews() {
        //实例化汉字转拼音类
        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();

        sideBar = (SideBar)v.findViewById(R.id.sidrbar);
        dialog = (TextView)v.findViewById(R.id.dialog);
        sideBar.setTextView(dialog);

        //设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                int position=mAdapter.getPositionForSection(s.charAt(0));
                if(position!=-1){
                    mRecyclerView.smoothScrollToPosition(position);   //应该是保持不动
                }
            }
        });

        SourceDateList = filledData(names);   //获取到源数据，其中包含名字与其首字母
        // 根据a-z进行排序源数据
        Collections.sort(SourceDateList, pinyinComparator);
        mRecyclerView = (RecyclerView)v.findViewById(R.id.id_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter = new Entirety_ContactsListAdapter(context,SourceDateList,mRecyclerView));

        mClearEditText = (ClearEditText)v.findViewById(R.id.filter_edit);
        mClearEditText.setHint(getString(R.string.searchedithintfirst)+names.size()+getString(R.string.searchedithintlast));
        //根据输入框输入值的改变来过滤搜索
        mClearEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
                selectDataDisplay(filterData(s.toString()));
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * 为ListView填充数据
     * @param date
     * @return
     */
    private List<SortModel> filledData(ArrayList<String> date){
        List<SortModel> mSortList = new ArrayList<SortModel>();

        for(int i=0; i<date.size(); i++){
            SortModel sortModel = new SortModel();
            sortModel.setName(date.get(i));
            //汉字转换成拼音
            String pinyin = characterParser.getSelling(date.get(i));
            String sortString = pinyin.substring(0, 1).toUpperCase();

            // 正则表达式，判断首字母是否是英文字母
            if(sortString.matches("[A-Z]")){
                sortModel.setSortLetters(sortString.toUpperCase());
            }else{
                sortModel.setSortLetters("#");
            }

            mSortList.add(sortModel);
        }
        return mSortList;
    }

    /**
     * 根据输入框中的值来过滤数据并更新ListView
     * @param filterStr
     */
    private List<SortModel> filterData(String filterStr){
        List<SortModel> filterDateList = new ArrayList<SortModel>();

        if(TextUtils.isEmpty(filterStr)){    //如果为空，则显示所有联系人
            filterDateList = SourceDateList;
        }else{
            filterDateList.clear();
            for(SortModel sortModel : SourceDateList){
                String name = sortModel.getName();
                /*
                **这一块有很大改进空间，更多的搜索算法和排序算法可以补充在这里
                 */
                if(name.indexOf(filterStr.toString()) != -1 || characterParser.getSelling(name).startsWith(filterStr.toString())){
                    filterDateList.add(sortModel);
                }
            }
        }
        return filterDateList;
    }
    public void selectDataDisplay(List<SortModel> filterDataList){
        // 根据a-z进行排序
        Collections.sort(filterDataList, pinyinComparator);
        mAdapter.updateListView(filterDataList);
    }

    @Override
    public void onStart(){
        super.onStart();
    }

}
