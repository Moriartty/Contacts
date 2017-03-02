package com.moriarty.user.contacts.Presenter;

import android.content.Context;

/**
 * Created by user on 17-2-13.
 */
public interface IQueryContacts {
    String[] queryContact(String personName, Context context);
    String getContactNameFromPhoneBook(Context context,String phone);
}
