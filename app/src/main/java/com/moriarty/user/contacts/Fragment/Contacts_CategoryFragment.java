package com.moriarty.user.contacts.Fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.moriarty.user.contacts.Activity.MainActivity;
import com.moriarty.user.contacts.Activity.Person_InfoCard;
import com.moriarty.user.contacts.Adapter.Category_TopListAdapter;
import com.moriarty.user.contacts.Adapter.Contacts_MoveListAdapter;
import com.moriarty.user.contacts.Adapter.Entirety_ContactsListAdapter;
import com.moriarty.user.contacts.Others.HandleContact;
import com.moriarty.user.contacts.Others.PopupMenuManager;
import com.moriarty.user.contacts.R;
import com.moriarty.user.contacts.Service.QueryContactsService;
import com.moriarty.user.contacts.Thread.AsyncImageLoader;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by user on 16-8-5.
 */
public class Contacts_CategoryFragment extends Fragment {
    private static final String currentTag="Contacts_CategoryFragment:";
    public ArrayList<String> groupName = new ArrayList<>();
    public ArrayList<ArrayList<String>> groupContent = new ArrayList<ArrayList<String>>();
    public ArrayList<String> collectInfo = new ArrayList<>();
    RecyclerView groupSettingView;
    private Category_TopListAdapter mAdapter;
    public static HashMap<String,String> headPortraits=new HashMap<>();
    private AsyncImageLoader asyncImageLoader;
    HandleContact handleContact;
    BroadcastReceiver receiver;
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final Context context = getActivity();
        groupName.clear();
        groupContent.clear();
        headPortraits.clear();
        handleContact=new HandleContact(context);
        //Log.d(MainActivity.TAG,currentTag+"handleContacts is created");
        final PopupMenuManager popupMenuManager = new PopupMenuManager(context);
        invalidateData();

        asyncImageLoader=new AsyncImageLoader(context);
        IntentFilter intentFilter=new IntentFilter("BC_FOUR");    //动态广播,接受来自GroupSettingService的广播
        receiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(HandleContact.alertDialog!=null)
                    HandleContact.alertDialog.dismiss();//关闭AlertDialog
            }
        };
        context.registerReceiver(receiver,intentFilter);

        Log.d("Moriarty","CategoryFragment is prepare to initView");

        View v = inflater.inflate(R.layout.fragment_viewpager1_layout1, null);
        groupSettingView = (RecyclerView) v.findViewById(R.id.groupsetting_recyclerview);
        groupSettingView.setLayoutManager(new GridLayoutManager(context, 3));
        groupSettingView.setAdapter(mAdapter = new Category_TopListAdapter(context));
        ExpandableListView list = (ExpandableListView) v.findViewById(R.id.list);
        list.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if(groupPosition==0)
                    return true;  //设置为true可以屏蔽点击事件
                else
                    return false;
            }
        });
        ExpandableListAdapter adapter = new BaseExpandableListAdapter() {

            private LayoutInflater layoutInflater = LayoutInflater.from(context);

            private TextView getTextView(int paddingvalue) {
                AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150);
                TextView textview = new TextView(context);
                textview.setLayoutParams(lp);
                textview.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
                textview.setPadding(paddingvalue, 0, 0, 0);
                textview.setTextSize(15);
                return textview;
            }

            @Override
            public int getGroupCount() {
                return groupName.size();
            }

            @Override
            public int getChildrenCount(int groupPosition) {
                return groupContent.get(groupPosition).size();
            }

            @Override
            public Object getGroup(int groupPosition) {
                return groupName.get(groupPosition);
            }

            @Override
            public Object getChild(int groupPosition, int childPosition) {
                return groupContent.get(groupPosition).get(childPosition);
            }

            @Override
            public long getGroupId(int groupPosition) {
                return groupPosition;
            }

            @Override
            public long getChildId(int groupPosition, int childPosition) {
                return childPosition;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }

            @Override
            public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
                if(groupPosition==0){
                    LinearLayout linearLayout=new LinearLayout(context);
                    TextView textView1=new TextView(context);
                    textView1.setText(getGroup(groupPosition).toString());
                    textView1.setTextSize(17);
                    textView1.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    linearLayout.addView(textView1);
                    convertView=linearLayout;
                }
                else {
                    convertView=layoutInflater.inflate(R.layout.item_category_parent,null);
                    TextView textView=(TextView)convertView.findViewById(R.id.category_parent_text);
                    TextView showNum=(TextView)convertView.findViewById(R.id.category_parent_shownum);
                    textView.setText(getGroup(groupPosition).toString());
                    showNum.setText(String.valueOf((groupContent.get(groupPosition)).size()));
                }
                return convertView;
            }

            @Override
            public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

                convertView = layoutInflater.inflate(R.layout.item_category_child, null);
                final ImageView child_img=(ImageView)convertView.findViewById(R.id.category_child_imageview);
                final TextView textView = (TextView) convertView.findViewById(R.id.category_child_textview);
                final Button movebutton=(Button)convertView.findViewById(R.id.category_chile_movebutton);
                //异步加载的关键步骤！！！
                final String imageUri=headPortraits.get(getChild(groupPosition, childPosition).toString());
                child_img.setTag(imageUri);
                final ImageView imageViewByTag=(ImageView)convertView.findViewWithTag(imageUri);
                Bitmap cachedImage=asyncImageLoader.loadDrawable(imageUri,"small",new AsyncImageLoader.ImageCallback() {
                    @Override
                    public void imageLoader(Bitmap imageDrawable, String imageUri) {
                        if(imageViewByTag!=null&&imageDrawable!=null){
                            imageViewByTag.setImageBitmap(imageDrawable);
                        }
                    }
                });
                if(cachedImage==null){
                    child_img.setImageResource(R.drawable.contact_default);
                }
                else{
                    child_img.setImageBitmap(cachedImage);
                }

                Log.d(MainActivity.TAG,currentTag+groupPosition);
                movebutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {        //移动联系人按钮的事件响应
                        popupMenuManager.showpopupMenu(movebutton, textView.getText().toString(),
                                child_img.getTag() == null ? null : child_img.getTag().toString(), layoutInflater,handleContact);

                    }
                });
                textView.setText(getChild(groupPosition, childPosition).toString());
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(context,Person_InfoCard.class);
                        intent.putExtra("info",textView.getText().toString());
                        intent.putExtra("flag",2);
                        context.startActivity(intent);
                    }
                });

                return convertView;
            }
            @Override
            public boolean isChildSelectable(int groupPosition, int childPosition) {
                return true;
            }
        };
        list.setAdapter(adapter);
        list.expandGroup(0);
        return v;
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        getContext().unregisterReceiver(receiver);
    }
    public void invalidateData(){
        ArrayList<String> temp=new ArrayList<>();
        temp.add(getResources().getString(R.string.my_collect));
        temp.addAll(MainActivity.groupName);
        groupName.addAll(temp);
        ArrayList<ArrayList<String>> temp2=new ArrayList<>();
        collectInfo.clear();
        collectInfo.addAll(MainActivity.collectedInfo);
        temp2.add(collectInfo);
        temp2.addAll(MainActivity.groupContent);
        groupContent.addAll(temp2);
        headPortraits.putAll(MainActivity.headPortraits);
    }

}
