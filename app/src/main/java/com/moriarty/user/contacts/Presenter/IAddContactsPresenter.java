package com.moriarty.user.contacts.Presenter;

import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

/**
 * Created by user on 17-2-25.
 */
public interface IAddContactsPresenter {
    void addGroupAction(View v);
    void selectGroupAction(Button selectGroupType, ArrayList<String> allGroupName);
    void confirmOthers();
    void confirmMyself(String sourceId);
}
