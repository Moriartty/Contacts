package com.moriarty.user.contacts.Thread;

import android.app.Activity;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;

import com.moriarty.user.contacts.Activity.MainActivity;
import com.moriarty.user.contacts.Others.ZoomBitmap;
import com.moriarty.user.contacts.R;
import com.moriarty.user.contacts.Service.QueryContactsService;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by user on 16-8-30.
 */
public class AsyncImageLoader {
    //Uri与bitmap的映射
    private HashMap<String,SoftReference<Bitmap>> imageCache;
    Context context;
    private static String currentTag="AsyncImageLoader:";

    public AsyncImageLoader(Context context){
        imageCache=new HashMap<>();
        this.context=context;
    }

    public Bitmap loadDrawable(final String imageUri,final String size, final ImageCallback imageCallback){
        if(imageCache.containsKey(imageUri)){
            SoftReference<Bitmap> softReference=imageCache.get(imageUri);
            Bitmap drawable=softReference.get();
            if(drawable!=null)
                return drawable;
        }
        final Handler handler=new Handler(){
            public void handleMessage(Message message){
                imageCallback.imageLoader((Bitmap) message.obj,imageUri);
            }
        };
        new Thread(){
            @Override
            public void run(){
                Bitmap drawable= loadImageFromUri(imageUri,size);
                imageCache.put(imageUri,new SoftReference<Bitmap>(drawable));
                Message message=handler.obtainMessage(0,drawable);
                handler.sendMessage(message);
            }
        }.start();
        return null;
    }

    public Bitmap loadBitmap(final String imageUrl,final ImageCallback imageCallback){   //加载联系人网络头像
        if(imageCache.containsKey(imageUrl)){
            SoftReference<Bitmap> softReference=imageCache.get(imageUrl);
            Bitmap drawable=softReference.get();
            Log.d("Moriarty","AsyncImageLoader:"+"multiplexing");
            if(drawable!=null)
                return drawable;
        }
        final Handler handler=new Handler(){
            public void handleMessage(Message message){
                imageCallback.imageLoader((Bitmap) message.obj,imageUrl);
            }
        };
        new Thread(){
            @Override
            public void run(){
                try{
                    Bitmap drawable= loadBitmapFromNet(imageUrl);
                    if(drawable==null){
                        Log.d("Moriarty","AsyncImageLoader:"+"new bitmap is null");
                    }
                    imageCache.put(imageUrl,new SoftReference<Bitmap>(drawable));
                    Message message=handler.obtainMessage(0,drawable);
                    handler.sendMessage(message);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }.start();
        return null;
    }

    public Bitmap loadBitmapFromNet(String imageUrl) throws IOException{
        URL url = new URL(imageUrl);
        Bitmap bitmap=null;
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestMethod("GET");
        if(conn.getResponseCode() == 200){
            InputStream inputStream = conn.getInputStream();
            if(inputStream!=null){
               // Log.d(MainActivity.TAG,currentTag+inputStream.toString());
                bitmap=ZoomBitmap.getZoomBitmap2(inputStream,context);
            }
            else
                Log.d(MainActivity.TAG,currentTag+"inputstream is null");
            //Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            return bitmap;
        }
        return null;
    }

    public Bitmap loadImageFromUri(String uri,String size){
        try{
            if(uri==null||uri.equals("None")||uri.equals("")){
                return null;
            }
            else
                return ZoomBitmap.getZoomBitmap(context,uri,size);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public interface ImageCallback{
        public void imageLoader(Bitmap imageDrawable, String imageUri);
    }

}
