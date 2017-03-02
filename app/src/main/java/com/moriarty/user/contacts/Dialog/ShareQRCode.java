package com.moriarty.user.contacts.Dialog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.moriarty.user.contacts.Others.TwoDimentionCode;
import com.moriarty.user.contacts.Others.ZoomBitmap;
import com.moriarty.user.contacts.R;

/**
 * Created by user on 16-11-3.
 */
public class ShareQRCode {
    public static void showQRCode(Context context,String name,String imageUri){
        AlertDialog alertDialog;
        View v4 = LayoutInflater.from(context).inflate(R.layout.dialog_generate_tdc, null);
        ImageView TDC_Image=(ImageView)v4.findViewById(R.id.two_dimension_code_image);
        //ImageView dialog_headrait=(ImageView)v4.findViewById(R.id.dialog_headrait);
        //TextView dialog_card_name=(TextView)v4.findViewById(R.id.dialog_card_name);
       /* dialog_card_name.setText(name);
        if(imageUri!=null&&!imageUri.equals("None")){
            dialog_headrait.setImageBitmap(ZoomBitmap.getZoomBitmap(context,imageUri,"small"));
        }
        else
            dialog_headrait.setImageDrawable(context.getResources().getDrawable(R.drawable.contact_default2));*/
        Bitmap bitmap=new TwoDimentionCode().generateTDC(name,ZoomBitmap.getZoomBitmap(context,imageUri,"small"));
        TDC_Image.setImageBitmap(bitmap);
        AlertDialog.Builder builder=new AlertDialog.Builder(v4.getContext()).setTitle(" ").setView(v4);
        alertDialog=builder.create();

        Window window=alertDialog.getWindow();   //设置背景为透明
        WindowManager.LayoutParams layoutParams=window.getAttributes();
        layoutParams.alpha=0.7f;
        window.setAttributes(layoutParams);
        alertDialog.show();
    }
}
