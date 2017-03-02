package com.moriarty.user.contacts.Presenter;

import android.os.Handler;
import android.os.Message;

import com.moriarty.user.contacts.Thread.ClientThread;

/**
 * Created by user on 17-2-13.
 */
public interface ILoadFragment {
    void initializeThread(Handler handler);
    void sendTiebaRefreshOrder(String tiebaUrl,String phone,String name);
    void sendWeiboRefreshOrder(String weiboUrl,String phone,String name);
    void receiveMessage(Message message,String tiebaId,String phoneText);
    void initializeFragment(Message message,String phone,String tiebaId);
}
