package com.moriarty.user.contacts.Presenter;

import android.os.Handler;
import android.os.Message;

import com.moriarty.user.contacts.Thread.RecommendThread;

/**
 * Created by user on 17-2-16.
 */
public interface IRecommendData {
    void handleData(Message message);
    void sendMessage(Handler handler);
    boolean initializeThread(Handler handler);
    RecommendThread getRecomThread();
    void initializeFragment();
    boolean isNetworkAvailable();
}
