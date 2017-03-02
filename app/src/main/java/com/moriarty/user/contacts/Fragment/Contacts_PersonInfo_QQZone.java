package com.moriarty.user.contacts.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moriarty.user.contacts.R;

/**
 * Created by user on 16-9-28.
 */
public class Contacts_PersonInfo_QQZone extends Fragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_qqzone, null);
        //TextView textView=(TextView)v.findViewById(R.id.)
        Log.d("Moriarty","Contacts_PersonInfo_QQZone:"+"prepare");

        return v;
    }
}
