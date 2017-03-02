package com.moriarty.user.contacts.Presenter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;

/**
 * Created by user on 17-2-23.
 */
public interface IMainPresenter {
    void skip2AddContactsView(ArrayList<String> myInfoList);
    void skip2PersonInfo(ArrayList<String> list);
    void handleReturnData(Message message);
    void inspectPermission(Handler handler);
    void voiceSearch(int requestCode, int resultCode, Intent data);
    void checkMySelfInfo(Handler handler);
}
