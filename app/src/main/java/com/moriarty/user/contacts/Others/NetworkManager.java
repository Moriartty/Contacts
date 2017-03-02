package com.moriarty.user.contacts.Others;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by user on 16-11-2.
 */
public class NetworkManager {

    public static boolean isNetworkAvailable(Context context)
    {
        ConnectivityManager mConnMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mMobile = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean flag = false;
        if((mWifi != null)  && ((mWifi.isAvailable()) || (mMobile.isAvailable())))
        {
            if((mWifi.isConnected()) || (mMobile.isConnected()))
            {
                flag = true;
            }
        }
        return flag;
    }


    public static boolean isConnectionAvailable(Context context)
    {
        boolean isConnectionFail = true;
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null)
        {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo == null || !activeNetworkInfo.isConnected())
            {
                isConnectionFail = true;
            }
            else
            {
                isConnectionFail = false;
            }
        }
        else
        {

        }
        return isConnectionFail;
    }




}
