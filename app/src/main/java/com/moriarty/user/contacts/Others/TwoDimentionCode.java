package com.moriarty.user.contacts.Others;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;

/**
 * Created by user on 16-10-9.
 */
public class TwoDimentionCode {
    private int QR_HEIGHT=600;
    private int QR_WIDTH=600;
    static String beginTag="BEGIN:VCARD";
    static String endTag="\n"+"END:VACRD";
    static String nameTag="\n"+"N:";
    static String telTag="\n"+"TEL:";
    static String emailTag="\n"+"EMAIL:";
    static String companyTag="\n"+"ORG:";
    static String titleTag="\n"+"TITLE:";
    static String bdayTag="\n"+"BDAY:";
    static String weiboTag="\n"+"URL:";
    static String adrTag="\n"+"ADR;HOME;POSTAL;PARCEL:";
    public Bitmap generateTDC(String name,Bitmap logoBm){          //二维码生成算法
        //String url=beginTag+"\n"+"N:"+name+endTag;   //联系人二维码名片格式
        String[] test=new String[]{"moriarty","13889446741","2992821771@qq.com","TCL","APP ENG","1995-12-13","www.weibo.cn/u/52341231",";;街道地址;惠州;广东;433330;中国"};
        String url=beginTag+nameTag+test[0]+telTag+test[1]+emailTag+test[2]+companyTag+test[3]+titleTag+test[4]+bdayTag+test[5]+weiboTag+test[6]+adrTag+test[7]+endTag;
        Bitmap bitmap=null;
        try{
            if(url.equals("")||url==null||url.length()<1){
                return null;
            }
            HashMap<EncodeHintType,Object> hints=new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET,"utf-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);  //容错率越高，二维码的像素点就越多
            BitMatrix bitMatrix=new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE,QR_WIDTH,QR_HEIGHT,hints);
            //Log.d("Moriarty","TwoDimentionCode:"+bitMatrix);
            int[] pixels=new int[QR_WIDTH*QR_HEIGHT];
            for(int y=0;y<QR_HEIGHT;y++){
                for(int x=0;x<QR_WIDTH;x++){
                    if(bitMatrix.get(x,y)){
                        pixels[y*QR_WIDTH+x]=0xff00bfff;

                    }
                    else{
                        pixels[y*QR_WIDTH+x]=0xffffffff;
                    }
                }
            }
            bitmap=Bitmap.createBitmap(QR_WIDTH,QR_HEIGHT,Bitmap.Config.RGB_565);
            bitmap.setPixels(pixels,0,QR_WIDTH,0,0,QR_WIDTH,QR_HEIGHT);


            if (logoBm != null) {
                bitmap = addLogo(bitmap, logoBm);   //将头像放置在二维码中央
            }
            return bitmap;
           // return bitmap != null && bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(a));
        }catch (Exception e){
            e.printStackTrace();
        }
        return bitmap;
    }


    private static Bitmap addLogo(Bitmap src, Bitmap logo) {
        if (src == null) {
            return null;
        }

        if (logo == null) {
            return src;
        }

        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();

        if (srcWidth == 0 || srcHeight == 0) {
            return null;
        }

        if (logoWidth == 0 || logoHeight == 0) {
            return src;
        }

        float scaleFactor = srcWidth * 1.0f / 5 / logoWidth;     //如何改成5以下的数值可能会导致无法识别,提高纠错码的级别可解决该问题
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.RGB_565);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(src, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);

            canvas.save(Canvas.ALL_SAVE_FLAG);
            canvas.restore();
        } catch (Exception e) {
            bitmap = null;
            e.getStackTrace();
        }


        return bitmap;
    }

    public static void shareImage(Bitmap bitmap, Context context) {   //关于分享图片至其他应用，设置完setAction,putExtra,setType后直接startActivity后系统就会罗列名单出来
        try {
            Uri uriToImage = Uri.parse(MediaStore.Images.Media.insertImage(
                    context.getContentResolver(), bitmap, null, null));
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uriToImage);
            shareIntent.setType("image/jpeg");
            context.startActivity(shareIntent);//需要用户选择哪种分享方式

            /*PackageManager packageManager = context.getPackageManager();  //搜索特定的应用来实现分享
            List<ResolveInfo> resolveInfoList = packageManager
                    .queryIntentActivities(shareIntent,
                            PackageManager.GET_INTENT_FILTERS);

            ComponentName componentName = null;
            for (int i = 0; i < resolveInfoList.size(); i++) {
                if (TextUtils.equals(
                        resolveInfoList.get(i).activityInfo.packageName,
                        "com.tencent.mm")) {
                    componentName = new ComponentName(
                            resolveInfoList.get(i).activityInfo.packageName,
                            resolveInfoList.get(i).activityInfo.name);
                    break;
                }
            }

            if (null != componentName) {
                shareIntent.setComponent(componentName);
                context.startActivity(shareIntent);
            } else {
               // ContextUtil.getInstance().showToastMsg("ÇëÏÈ°²×°**");
            }*/
        } catch (Exception e) {
            //ContextUtil.getInstance().showToastMsg("·ÖÏíÍŒÆ¬µœ**Ê§°Ü");
        }
    }

}
