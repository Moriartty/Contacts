package com.moriarty.user.contacts.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.moriarty.user.contacts.Activity.Tieba_DisplayActivity;
import com.moriarty.user.contacts.Fragment.Contacts_PersonInfo_Tieba;
import com.moriarty.user.contacts.R;
import com.moriarty.user.contacts.Thread.AsyncImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by user on 16-10-24.
 */
public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.MyViewHolder>{
    private Context mcontext;
    private LayoutInflater myLayoutInflater;
    RecyclerView gallery;
    ArrayList<String> imgs_url;
    AsyncImageLoader asyncImageLoader;
    String img_url;
    public GalleryAdapter(Context context,ArrayList<String> imgs_url,RecyclerView gallery){
        mcontext=context;
        myLayoutInflater=LayoutInflater.from(mcontext);
        this.imgs_url=imgs_url;
        this.gallery=gallery;
        asyncImageLoader=new AsyncImageLoader(mcontext);
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = myLayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery, parent, false);
        final MyViewHolder holder=new MyViewHolder(view);
        return holder;
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        img_url=imgs_url.get(position);
        holder.item_gallery.setTag(img_url);
        Bitmap cachedImage=asyncImageLoader.loadBitmap(img_url,new AsyncImageLoader.ImageCallback() {
            @Override
            public void imageLoader(Bitmap imageDrawable, String imageUri) {
                ImageView imageViewByTag=(ImageView)gallery.findViewWithTag(imageUri);
                if(imageViewByTag!=null&&imageDrawable!=null){
                    imageViewByTag.setImageBitmap(imageDrawable);
                }
            }
        });
        if(cachedImage!=null){
            holder.item_gallery.setImageBitmap(cachedImage);
        }
    }

    @Override
    public int getItemCount() {
        return imgs_url == null ? 0 : imgs_url.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.item_gallery)
        ImageView item_gallery;
        public MyViewHolder(View view){
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
