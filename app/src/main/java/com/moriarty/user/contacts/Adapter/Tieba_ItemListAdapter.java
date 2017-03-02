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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.moriarty.user.contacts.Activity.Tieba_DisplayActivity;
import com.moriarty.user.contacts.Fragment.Contacts_PersonInfo_Tieba;
import com.moriarty.user.contacts.R;
import com.moriarty.user.contacts.Thread.AsyncImageLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * Created by user on 16-10-19.
 */
public class Tieba_ItemListAdapter extends RecyclerView.Adapter<Tieba_ItemListAdapter.MyViewHolder> {
    private Context mcontext;
    private LayoutInflater myLayoutInflater;
    private ArrayList<HashMap.Entry<String,HashMap<String,String>>> tieba_sortlist;
    private String id;    //记录联系人的贴吧id
    private String imageUrl;   //记录联系人的头像网络路径
    private AsyncImageLoader asyncImageLoader;
    private HashMap<String,String> other_info;
    RecyclerView tieba_list;
    public Tieba_ItemListAdapter(Context context,ArrayList<HashMap.Entry<String,HashMap<String,String>>> tieba_sortlist,HashMap<String,String> other_info,RecyclerView tieba_list){
        mcontext=context;
        myLayoutInflater=LayoutInflater.from(mcontext);
        asyncImageLoader=new AsyncImageLoader(mcontext);
        this.tieba_list=tieba_list;
        this.other_info=other_info;
        imageUrl=other_info.get("head_img");
        this.tieba_sortlist=tieba_sortlist;
        this.id=other_info.get("name");
        Contacts_PersonInfo_Tieba.tieba_map.clear();
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tiebalist_content, parent, false);
        final MyViewHolder holder=new MyViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(mcontext,holder.tieba_person_name.getTag().toString(),Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(mcontext, Tieba_DisplayActivity.class);
                HashMap<String,String> pre_map=new HashMap<String, String>();
                pre_map.put("id",holder.tieba_person_name.getText().toString());
                pre_map.put("time",holder.tieba_note_time.getText().toString());
                pre_map.put("address",holder.tieba_note_address.getText().toString());
                pre_map.put("reply_content",holder.tieba_note_reply_content.getText().toString());
                pre_map.put("head_img",imageUrl);
                pre_map.put("url",holder.tieba_person_name.getTag().toString());
                Log.d("Moriarty","Tieba_ItemListAdapter:"+holder.tieba_person_name.getTag().toString());
                Bundle bundle=new Bundle();
                bundle.putSerializable("pre_map",pre_map);
                intent.putExtras(bundle);
                mcontext.startActivity(intent);
            }
        });
        return holder;
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.img.setTag(imageUrl+position);
        Bitmap cachedImage=asyncImageLoader.loadBitmap(imageUrl,new AsyncImageLoader.ImageCallback() {
            @Override
            public void imageLoader(Bitmap imageDrawable, String imageUri) {
                ImageView imageViewByTag=(ImageView)tieba_list.findViewWithTag(imageUrl+position);
                if(imageViewByTag!=null&&imageDrawable!=null){
                    imageViewByTag.setImageBitmap(imageDrawable);
                }
            }
        });
        if(cachedImage==null){
            holder.img.setImageResource(R.drawable.contact_default);
        }
        else{
            holder.img.setImageBitmap(cachedImage);
        }

        holder.tieba_person_name.setText(id);
        //将每条帖子的原文链接地址url用setTag()绑定到每个item的名字上,然后在点击事件中获取tag.
        holder.tieba_person_name.setTag(tieba_sortlist.get(position).getValue().get("url"));
        if(tieba_sortlist.get(position).getValue().containsKey("title")){    //发帖
            holder.tieba_note_reply_content.setText(tieba_sortlist.get(position).getValue().get("title"));
            holder.tieba_note_reply.setText(tieba_sortlist.get(position).getValue().get("content"));
        }
        else {              //回复
            holder.tieba_note_reply_content.setText(tieba_sortlist.get(position).getValue().get("reply_content"));
            holder.tieba_note_reply.setText(tieba_sortlist.get(position).getValue().get("titletxt"));
        }
        holder.tieba_note_time.setText(tieba_sortlist.get(position).getValue().get("time"));
        holder.tieba_note_address.setText(tieba_sortlist.get(position).getValue().get("address"));
    }

    @Override
    public int getItemCount() {
        return tieba_sortlist == null ? 0 : tieba_sortlist.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tieba_person_name)
        TextView tieba_person_name;
        @Bind(R.id.tieba_note_time)
        TextView tieba_note_time;
        @Bind(R.id.tieba_note_reply_content)
        TextView tieba_note_reply_content;
        @Bind(R.id.tieba_note_reply)
        TextView tieba_note_reply;
        @Bind(R.id.tieba_note_address)
        TextView tieba_note_address;
        @Bind(R.id.tieba_head_img)
        ImageView img;
        public MyViewHolder(View view){
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
