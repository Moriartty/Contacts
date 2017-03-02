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

import com.moriarty.user.contacts.Activity.MainActivity;
import com.moriarty.user.contacts.Activity.Person_InfoCard;
import com.moriarty.user.contacts.Activity.Tieba_DisplayActivity;
import com.moriarty.user.contacts.Fragment.Contacts_EntiretyFragment;
import com.moriarty.user.contacts.Fragment.Contacts_PersonInfo_Tieba;
import com.moriarty.user.contacts.Fragment.Contacts_ValueSortFragment;
import com.moriarty.user.contacts.Presenter.IQueryContacts;
import com.moriarty.user.contacts.Presenter.QueryContacts;
import com.moriarty.user.contacts.R;
import com.moriarty.user.contacts.Thread.AsyncImageLoader;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by user on 16-11-12.
 */
public class SPDataCollectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    public static final String TAG ="Moriarty" ;
    private static final String currentTag="SPDataAdapter:";
    Context mContext;
    String imageUri;
    AsyncImageLoader asyncImageLoader;
    RecyclerView recyclerView;

    private final int EMPTY_VIEW=0;
    private final int VIEW1=1;
    private final int VIEW2=2;
    private ArrayList<HashMap.Entry<String,HashMap<String,String>>> all_sortList;
    public SPDataCollectionAdapter(Context context,ArrayList<HashMap.Entry<String,HashMap<String,String>>> all_sortlist,RecyclerView recyclerView){
        this.mContext=context;
        this.all_sortList=all_sortlist;
        asyncImageLoader=new AsyncImageLoader(mContext);
        this.recyclerView=recyclerView;
        if(all_sortlist!=null)
            Log.d(MainActivity.TAG,currentTag+" all_sortList size is "+all_sortlist.size());
        else
            Log.d(MainActivity.TAG,currentTag+"all_sortList is null");
    }

    @Override
    public int getItemViewType(int position) {
        if(all_sortList==null||all_sortList.size()==0){
           // Log.d(MainActivity.TAG,currentTag+" empty");
            return EMPTY_VIEW;
        }

        if(all_sortList.get(position).getValue().containsValue("Tieba")){
           // Log.d(MainActivity.TAG,currentTag+" tieba");
            return VIEW1;
        }
        else if(all_sortList.get(position).getValue().containsValue("Weibo")){
           // Log.d(MainActivity.TAG,currentTag+" weibo");
            return VIEW2;
        }
        return EMPTY_VIEW;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType){
            case EMPTY_VIEW:
                view=LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_viewpager3_layout1,parent,false);
                final RecyclerView.ViewHolder holder=new EmptyViewHolder(view);
                return holder;
                //break;
            case VIEW2:
                view=LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weibolist_content,parent,false);
                final WeiboViewHolder holder1=new WeiboViewHolder(view);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(mContext, Person_InfoCard.class);
                        intent.putExtra("info",holder1.weibo_person_name.getText().toString());
                        intent.putExtra("flag",2);
                        mContext.startActivity(intent);
                    }
                });
                return holder1;
                //break;
            case VIEW1:
                view=LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tiebalist_content,parent,false);
                final TiebaViewHolder holder2=new TiebaViewHolder(view);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(mContext, Person_InfoCard.class);
                        intent.putExtra("info",holder2.tieba_person_name.getText().toString());
                        intent.putExtra("flag",2);
                        mContext.startActivity(intent);
                    }
                });
                return holder2;
                //break;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        //Log.d(TAG,currentTag+position+" "+holder.getItemViewType());
        if(holder instanceof TiebaViewHolder){
            //Log.d(MainActivity.TAG,currentTag+" "+position);
            TiebaViewHolder myViewHolder = (TiebaViewHolder)holder;
            imageUri= all_sortList.get(position).getValue().get("head_img");
            getHeadPic(myViewHolder.img,imageUri,position);

            myViewHolder.tieba_person_name.setText(all_sortList.get(position).getValue().get("id"));
            //将每条帖子的原文链接地址url用setTag()绑定到每个item的名字上,然后在点击事件中获取tag.
            myViewHolder.tieba_person_name.setTag(all_sortList.get(position).getValue().get("url"));
            if(all_sortList.get(position).getValue().containsKey("title")){    //发帖
                myViewHolder.tieba_note_reply_content.setText(all_sortList.get(position).getValue().get("title"));
                myViewHolder.tieba_note_reply.setText(all_sortList.get(position).getValue().get("content"));
            }
            else {              //回复
                myViewHolder.tieba_note_reply_content.setText(all_sortList.get(position).getValue().get("reply_content"));
                myViewHolder.tieba_note_reply.setText(all_sortList.get(position).getValue().get("titletxt"));
            }
            myViewHolder.tieba_note_time.setText(all_sortList.get(position).getValue().get("time"));
            myViewHolder.tieba_note_address.setText(all_sortList.get(position).getValue().get("address"));

        }
        else if(holder instanceof WeiboViewHolder){
            WeiboViewHolder weiboHolder=(WeiboViewHolder)holder;
            imageUri=all_sortList.get(position).getValue().get("content_img");
            Log.d("Moriarty",currentTag+imageUri);
            if(imageUri!=null&&!imageUri.equals("")){
                Log.d(MainActivity.TAG,currentTag+position);
                weiboHolder.weibo_note_img.setVisibility(View.VISIBLE);
                weiboHolder.weibo_note_img.setTag(position);
                Bitmap cachedImage1=asyncImageLoader.loadBitmap(imageUri,new AsyncImageLoader.ImageCallback() {
                    @Override
                    public void imageLoader(Bitmap imageDrawable, String imageUri) {
                        ImageView imageViewByTag=(ImageView)recyclerView.findViewWithTag(position);
                        if(imageViewByTag!=null&&imageDrawable!=null){
                            imageViewByTag.setImageBitmap(imageDrawable);
                        }
                    }
                });
                if(cachedImage1!=null){
                    weiboHolder.weibo_note_img.setImageBitmap(cachedImage1);
                }
                else {
                    weiboHolder.weibo_note_img.setImageDrawable(mContext.getResources().getDrawable(R.drawable.transparent));
                }
            }
            //图片错位问题解决，单单设置tag只能保证需要显示图片的view能正确显示图片，
            //无法保证不需要显示图片的地方不显示图片，所以需要设置不需要显示图片的view的visibility为gone.
            else
                weiboHolder.weibo_note_img.setVisibility(View.GONE);
            final String headimg_url=all_sortList.get(position).getValue().get("head_img");
            getHeadPic(weiboHolder.img,headimg_url,position);

            weiboHolder.weibo_person_name.setText(all_sortList.get(position).getValue().get("id"));
            weiboHolder.weibo_note_content.setText(all_sortList.get(position).getValue().get("content_text"));
            weiboHolder.weibo_note_time.setText(all_sortList.get(position).getValue().get("time"));
            weiboHolder.weibo_note_focus.setText(all_sortList.get(position).getValue().get("focus"));
        }
        else if(holder instanceof EmptyViewHolder){
            EmptyViewHolder emptyViewHolder=(EmptyViewHolder)holder;
            emptyViewHolder.awoke_text.setText(mContext.getResources().getString(R.string.connected_filed_tips));
        }
    }

    @Override
    public int getItemCount() {
        return all_sortList == null ? 1 : all_sortList.size();
    }

    public class WeiboViewHolder extends RecyclerView.ViewHolder {
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
        public WeiboViewHolder(View view){
            super(view);
            ButterKnife.bind(this, view);
        }
    }
    public class TiebaViewHolder extends RecyclerView.ViewHolder{
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
        public TiebaViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }

    private void getHeadPic(ImageView imageView,final String headimg_url,final int position){
        imageView.setTag(headimg_url+position);
        Bitmap cachedImage2=asyncImageLoader.loadBitmap(headimg_url,new AsyncImageLoader.ImageCallback() {
            @Override
            public void imageLoader(Bitmap imageDrawable, String imageUri) {
                ImageView imageViewByTag=(ImageView)recyclerView.findViewWithTag(headimg_url+position);
                if(imageViewByTag!=null&&imageDrawable!=null){
                    imageViewByTag.setImageBitmap(imageDrawable);
                }
            }
        });
        if(cachedImage2!=null){
            imageView.setImageBitmap(cachedImage2);
        }
        if(cachedImage2==null){
            imageView.setImageResource(R.drawable.contact_default);
        }
    }

    public class EmptyViewHolder extends RecyclerView.ViewHolder{
        @Bind(R.id.awoke_text)
        TextView awoke_text;
        public EmptyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
