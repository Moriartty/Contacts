package com.moriarty.user.contacts.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moriarty.user.contacts.R;

/**
 * Created by user on 16-8-13.
 */
public class Contacts_NoContactFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_viewpager3_layout1, null);
        TextView awoke_text=(TextView)v.findViewById(R.id.awoke_text);
        awoke_text.setText(getString(R.string.no_phone_details));
        return v;
    }
}
