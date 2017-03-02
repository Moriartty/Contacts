package com.moriarty.user.contacts.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.moriarty.user.contacts.Activity.Person_InfoCard;
import com.moriarty.user.contacts.Fragment.Contacts_PersonInfo_QQZone;
import com.moriarty.user.contacts.Fragment.Contacts_PersonInfo_Tieba;
import com.moriarty.user.contacts.Fragment.Contacts_PersonInfo_Weibo;

/**
 * Created by user on 16-9-28.
 */
public class PagerAdapter_netInfo extends FragmentStatePagerAdapter {
    public PagerAdapter_netInfo(FragmentManager fm,String tel)
    {
        super(fm);
    }
    @Override
    public Fragment getItem(int position)
    {
        switch(position){
            case 0:return new Contacts_PersonInfo_Tieba();
            case 1:return new Contacts_PersonInfo_Weibo();
            default:return new Contacts_PersonInfo_QQZone();
        }
    }
    @Override
    public int getCount()
    {
        return 3;
    }
}
