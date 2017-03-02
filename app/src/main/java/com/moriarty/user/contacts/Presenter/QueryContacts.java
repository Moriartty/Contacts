package com.moriarty.user.contacts.Presenter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import com.moriarty.user.contacts.Activity.MainActivity;
import com.moriarty.user.contacts.Service.QueryContactsService;

/**
 * Created by user on 17-2-13.
 */
public class QueryContacts implements IQueryContacts {
    private final static String currentTag="Person_InfCard:";
    @Override
    public String[] queryContact(String personName, Context context) {
        return queryContactInPerson_InfoView(personName,context);
    }

    private String[] queryContactInPerson_InfoView(String personName,Context context){
        String[] detail=new String[6];
        Cursor cursor=context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null,"display_name=?",new String[]{personName},null);
        cursor.moveToNext();
        String ContactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));   //获取联系人id
        Log.d(MainActivity.TAG,currentTag+"ContactId="+ContactId);
        Cursor phones=context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID+"="+ContactId,null,null);

        while(phones.moveToNext())
        {
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            //Log.d("Moriarty",":"+phoneNumber);
            detail[0]=phoneNumber;
        }
        phones.close();
        Cursor emails=context.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                null,ContactsContract.CommonDataKinds.Email.CONTACT_ID+"="+ContactId,null,null);
        while(emails.moveToNext())
        {
            String emailAddress=emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
            //Log.d("Moriarty",":"+emailAddress);
            detail[1]=emailAddress;
        }
        emails.close();
        detail[2]= QueryContactsService.getTiebaId(personName);
        detail[3]=QueryContactsService.getTiebaUrl(personName);
        detail[4]=QueryContactsService.getWeiboId(personName);
        detail[5]=QueryContactsService.getWeiboUrl(personName);
        return detail;
    }

    @Override
    public String getContactNameFromPhoneBook(Context context, String phoneNum) {
        String contactName = "";
        ContentResolver cr = context.getContentResolver();
        Cursor pCur = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?",
                new String[] { phoneNum }, null);
        if (pCur.moveToFirst()) {
            contactName = pCur
                    .getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            pCur.close();
        }
        return contactName;
    }
}
