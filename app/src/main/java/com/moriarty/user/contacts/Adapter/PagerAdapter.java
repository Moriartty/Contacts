package com.moriarty.user.contacts.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.moriarty.user.contacts.Activity.MainActivity;
import com.moriarty.user.contacts.Fragment.Contacts_CategoryFragment;
import com.moriarty.user.contacts.Fragment.Contacts_EntiretyFragment;
import com.moriarty.user.contacts.Fragment.Contacts_NoContactFragment;
import com.moriarty.user.contacts.Fragment.Contacts_ValueSortFragment;

/**
 * Created by user on 16-8-19.
 */
public class PagerAdapter extends FragmentStatePagerAdapter {
    public PagerAdapter(FragmentManager fm)
    {
        super(fm);
    }
    @Override
    public Fragment getItem(int position)
    {
        if(MainActivity.names.size()==0&&MainActivity.details.size()==0)
        {
           // Log.d("Moriarty","PagerAdapter:"+MainActivity.names.size());
            switch(position){
                case 1:return new Contacts_CategoryFragment();
                default:return new Contacts_NoContactFragment();
            }
        }
        else{
            switch(position){
                case 0:return new Contacts_EntiretyFragment();
                case 1:return new Contacts_CategoryFragment();
                default:return new Contacts_ValueSortFragment();
            }
        }
    }
    @Override
    public int getCount()
    {
        return MainActivity.addresses.length;
    }
}
