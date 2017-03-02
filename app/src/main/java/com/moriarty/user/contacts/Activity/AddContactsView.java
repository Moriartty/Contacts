package com.moriarty.user.contacts.Activity;

import android.content.ServiceConnection;

import java.util.ArrayList;

/**
 * Created by user on 17-2-25.
 */
public interface AddContactsView {
    void unBindServiceConn(ServiceConnection connection);
    void showToast(String s);
    void destroy();
    int getFlag();
    String getPhoneText();
    ArrayList<String> getHistory();
}
