package com.moriarty.user.contacts.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.moriarty.user.contacts.Activity.Tieba_DisplayActivity;
import com.moriarty.user.contacts.Fragment.Contacts_PersonInfo_Tieba;
import com.moriarty.user.contacts.Fragment.Contacts_PersonInfo_Weibo;
import com.moriarty.user.contacts.R;
import com.moriarty.user.contacts.Thread.AsyncImageLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by user on 16-10-25.
 */
public class Weibo_ItemListAdapter extends RecyclerView.Adapter<Weibo_ItemListAdapter.MyViewHolder> {
    private Context mcontext;
    private LayoutInflater myLayoutInflater;
    private String id;    //记录联系人的微博id
    private String imageUrl;
    private String headimg_url;  //记录联系人的微博头像路径
    private AsyncImageLoader asyncImageLoader;
    private HashMap<String,String> other_info=new HashMap<>();
    RecyclerView weibo_list;
    private ArrayList<HashMap.Entry<String,HashMap<String,String>>> weibo_sortlist;
    public Weibo_ItemListAdapter(Context context,ArrayList<HashMap.Entry<String,HashMap<String,String>>> weibo_sortlist,HashMap<String,String> other_info,RecyclerView weibo_list){
        mcontext=context;
        myLayoutInflater=LayoutInflater.from(mcontext);
        asyncImageLoader=new AsyncImageLoader(context);
        this.weibo_list=weibo_list;
        this.other_info=other_info;
        this.id=other_info.get("name").split(" ")[0];
        //weibo_map.remove("other_info");
        //this.weibo_sortlist=sortByTime(weibo_map);
        this.weibo_sortlist=weibo_sortlist;
        Contacts_PersonInfo_Weibo.weibo_map.clear();
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weibolist_content, parent, false);
        final MyViewHolder holder=new MyViewHolder(view);
        return holder;
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        imageUrl=weibo_sortlist.get(position).getValue().get("content_img");
        Log.d("Moriarty","Weibo_ItemListAdapter:"+imageUrl);
        if(imageUrl!=null&&!imageUrl.equals("")){
            Log.d("Moriarty","Weibo_ItemListAdapter:"+position);
            holder.weibo_note_img.setVisibility(View.VISIBLE);
            holder.weibo_note_img.setTag(position);
            Bitmap cachedImage1=asyncImageLoader.loadBitmap(imageUrl,new AsyncImageLoader.ImageCallback() {
                @Override
                public void imageLoader(Bitmap imageDrawable, String imageUri) {
                    ImageView imageViewByTag=(ImageView)weibo_list.findViewWithTag(position);
                    if(imageViewByTag!=null&&imageDrawable!=null){
                        imageViewByTag.setImageBitmap(imageDrawable);
                    }
                }
            });
            if(cachedImage1!=null){
                holder.weibo_note_img.setImageBitmap(cachedImage1);
            }
            else {
                holder.weibo_note_img.setImageDrawable(mcontext.getResources().getDrawable(R.drawable.transparent));
            }
        }
        //图片错位问题解决，单单设置tag只能保证需要显示图片的view能正确显示图片，无法保证不需要显示图片的地方不显示图片，所以需要设置不需要显示图片的view的visibility为gone.
        else
            holder.weibo_note_img.setVisibility(View.GONE);
        headimg_url=other_info.get("head_img");
        holder.img.setTag(headimg_url+position);
        Bitmap cachedImage2=asyncImageLoader.loadBitmap(headimg_url,new AsyncImageLoader.ImageCallback() {
            @Override
            public void imageLoader(Bitmap imageDrawable, String imageUri) {
                ImageView imageViewByTag=(ImageView)weibo_list.findViewWithTag(headimg_url+position);
                if(imageViewByTag!=null&&imageDrawable!=null){
                    imageViewByTag.setImageBitmap(imageDrawable);
                }
            }
        });
        if(cachedImage2!=null){

            holder.img.setImageBitmap(cachedImage2);
        }

        holder.weibo_person_name.setText(id);
        holder.weibo_note_content.setText(weibo_sortlist.get(position).getValue().get("content_text"));
        holder.weibo_note_time.setText(weibo_sortlist.get(position).getValue().get("time"));
        holder.weibo_note_focus.setText(weibo_sortlist.get(position).getValue().get("focus"));
    }

    @Override
    public int getItemCount() {
        return weibo_sortlist == null ? 0 : weibo_sortlist.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.weibo_person_name)
        TextView weibo_person_name;
        @Bind(R.id.weibo_note_time)
        TextView weibo_note_time;
        @Bind(R.id.weibo_note_content)
        TextView weibo_note_content;
        @Bind(R.id.weibo_note_img)
        ImageView weibo_note_img;
        @Bind(R.id.weibo_head_img)
        ImageView img;
        @Bind(R.id.weibo_note_focus)
        TextView weibo_note_focus;
        public MyViewHolder(View view){
            super(view);
            ButterKnife.bind(this, view);
        }
    }
   /* private ArrayList<HashMap.Entry<String,HashMap<String,String>>> sortByTime(HashMap<String,HashMap<String,String>> weibo_map){
        ArrayList<HashMap.Entry<String,HashMap<String,String>>> tieba_sortlist=new ArrayList<>(weibo_map.entrySet());
        Collections.sort(tieba_sortlist, new Comparator<HashMap.Entry<String, HashMap<String, String>>>() {
            @Override
            public int compare(HashMap.Entry<String, HashMap<String, String>> lhs, HashMap.Entry<String, HashMap<String, String>> rhs) {
                return rhs.getValue().get("time").toString().compareTo(lhs.getValue().get("time").toString());
            }
        });
        return tieba_sortlist;
    }*/
}
