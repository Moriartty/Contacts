package com.moriarty.user.contacts.Activity;

/**
 * Created by user on 17-2-23.
 */
public interface MainView {
    void showToast(String s);
    void invalidate();
    void enduePermission(String[] permission);
    void setCurrentItem(int flag);
}
