package com.moriarty.user.contacts.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.moriarty.user.contacts.Adapter.GalleryAdapter;
import com.moriarty.user.contacts.Adapter.Tieba_ItemListAdapter;
import com.moriarty.user.contacts.Others.SignalManager;
import com.moriarty.user.contacts.Others.XmlToMap;
import com.moriarty.user.contacts.R;
import com.moriarty.user.contacts.Thread.AsyncImageLoader;
import com.moriarty.user.contacts.Thread.DisplayTiebaDetailTask;
import com.moriarty.user.contacts.User_Defind.DividerItemDecoration;

import org.dom4j.DocumentException;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

/**
 * Created by user on 16-10-24.
 */
public class Tieba_DisplayActivity extends AppCompatActivity {
    private static final String currentTag="Tieba_DisplayActivity:";
    ImageView tieba_head_img;
    TextView tieba_person_name;
    TextView tieba_note_time;
    TextView tieba_note_reply;
    TextView tieba_note_reply_content;
    TextView tieba_note_address;
    String url;
    Handler handler;
    ProgressDialog progressDialog;
    StringBuffer stringBuffer=new StringBuffer();
    String returnValue;
    HashMap<String,String> map;
    HashMap<String,String> pre_map;
    ArrayList<String> topic_imgs_url=new ArrayList<>();
    ArrayList<String> reply_imgs_url=new ArrayList<>();
    RecyclerView topic_gallery;
    RecyclerView reply_gallery;
    GalleryAdapter topic_galleryAdapter;
    GalleryAdapter reply_galleryAdapter;
    AsyncImageLoader asyncImageLoader;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tieba_content_main);

        handler=new Handler(){
            @Override
            public void handleMessage(Message message){
                if(message.what== SignalManager.return_TiebaDetail_signal){
                    progressDialog.dismiss();  //关于这段代码的放置位置有待商榷
                    stringBuffer.append(message.obj.toString()+"\n");
                    if(message.obj.toString().equals("</tieba>")){
                        //progressDialog.dismiss();
                        returnValue=stringBuffer.toString();
                        stringBuffer.setLength(0);
                        try{
                            map=new HashMap(XmlToMap.xml2map(returnValue,false));
                            getImgs_url(map);
                            invalidate(pre_map,map);
                        }catch (DocumentException e){
                            e.printStackTrace();
                        }
                        Log.d(MainActivity.TAG,currentTag+returnValue);
                    }
                }
            }
        };

        tieba_head_img=(ImageView)findViewById(R.id.tieba_main_head_img);
        tieba_person_name=(TextView)findViewById(R.id.tieba_main_person_name);
        tieba_note_time=(TextView)findViewById(R.id.tieba_note_main_time);
        tieba_note_reply=(TextView)findViewById(R.id.tieba_note_main_reply);
        tieba_note_reply_content=(TextView)findViewById(R.id.tieba_note_main_reply_content);
        tieba_note_address=(TextView)findViewById(R.id.tieba_note_main_address);
        topic_gallery=(RecyclerView)findViewById(R.id.tieba_topic_gallery);
        reply_gallery=(RecyclerView)findViewById(R.id.tieba_reply_gallery);
        progressDialog=new ProgressDialog(this);

        pre_map=(HashMap<String, String>) getIntent().getExtras().getSerializable("pre_map");
        url=pre_map.get("url").toString();
        Log.d(MainActivity.TAG,currentTag+url);

        DisplayTiebaDetailTask displayTiebaDetailTask=new DisplayTiebaDetailTask(this,url,handler,progressDialog);
        displayTiebaDetailTask.execute();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(pre_map.get("id"));  //将联系人贴吧id设为toolbar上的标题
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);
        asyncImageLoader=new AsyncImageLoader(this);
    }

    private void getImgs_url(HashMap<String,String> map){  //使用迭代取出hashMap中的图片路径并按类别存放
        Iterator<HashMap.Entry<String,String>> iterator=map.entrySet().iterator();
        while(iterator.hasNext()){
            HashMap.Entry<String,String> temp=iterator.next();
            if(temp.getKey().contains("topic_img"))   //title的主题图片
                topic_imgs_url.add(temp.getValue());
            else if(temp.getKey().contains("reply_img")) //reply_content的主题图片
                reply_imgs_url.add(temp.getValue());
        }
    }

    public void invalidate(HashMap<String,String> pre_map,HashMap<String,String> map){
        tieba_person_name.setText(pre_map.get("id"));
        tieba_note_time.setText(pre_map.get("time"));
        tieba_note_address.setText(pre_map.get("address"));
        if(map.get("content_text")==null||map.get("content_text").equals(""))
            tieba_note_reply_content.setText(pre_map.get("reply_content"));
        else
            tieba_note_reply_content.setText(map.get("content_text"));
        tieba_note_reply.setText(map.get("topic_text"));
        getSingleBitmapFromNet(pre_map.get("head_img"));

        if(reply_imgs_url.size()!=0){
            reply_galleryAdapter=new GalleryAdapter(this,reply_imgs_url,reply_gallery);
            reply_gallery.setLayoutManager(new GridLayoutManager(this,3));
            reply_gallery.setAdapter(reply_galleryAdapter);
            reply_gallery.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.HORIZONTAL_LIST));
            reply_gallery.setVisibility(View.VISIBLE);
        }

        if(topic_imgs_url.size()!=0){
            topic_galleryAdapter=new GalleryAdapter(this,topic_imgs_url,topic_gallery);
            topic_gallery.setLayoutManager(new GridLayoutManager(this,3));
            topic_gallery.setAdapter(topic_galleryAdapter);
            topic_gallery.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.HORIZONTAL_LIST));
            topic_gallery.setVisibility(View.VISIBLE);
        }
    }

    public void getSingleBitmapFromNet(final String url){
        final Handler handler=new Handler(){
            @Override
            public void handleMessage(Message message){
                if(message.what==SignalManager.return_SingleImg_signal){
                    tieba_head_img.setImageBitmap((Bitmap) message.obj);
                }
            }
        };
        new Thread(){
            @Override
            public void run(){
                try{
                    Bitmap drawable= asyncImageLoader.loadBitmapFromNet(url);
                    Message message=new Message();
                    message.obj=drawable;
                    message.what=SignalManager.return_SingleImg_signal;
                    handler.sendMessage(message);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
