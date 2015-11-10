package com.example.kevinhoa95.chansms;

/**
 * Created by kevinhoa95 on 10/7/2015.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;
import android.database.sqlite.SQLiteDatabase;

import static android.database.sqlite.SQLiteDatabase.CREATE_IF_NECESSARY;
import static android.database.sqlite.SQLiteDatabase.openDatabase;

public class MySmsReceive extends BroadcastReceiver{
    public static final String SMS_EXTRA = "pdus";
    SmsMessage sms;
    Object []smsExtra;
    String address;
    String message ="";

    @Override

    //Lắng nghe tin nhắn đến
    public void onReceive(Context context, Intent intent){
        Bundle extras = intent.getExtras();
        String number="0";
        if(extras != null){
            smsExtra = (Object[])extras.get(SMS_EXTRA);
            for (int i = 0; i < smsExtra.length; i++)
            {
                sms = SmsMessage.createFromPdu((byte[])smsExtra[i]);
                address = sms.getOriginatingAddress();
                if (address.charAt(0) == '+' && address.charAt(1) == '8' && address.charAt(2) == '4'){
                    for (int j = 3; j < address.length(); j++ ) number += address.charAt(j);
                }
                XoaSms(context, number);
            }
        }
    }
    //kiểm tra xem có phải số trong danh sách chặn hay không!
    public boolean checkDumpNumber(Context context, String phoneNumber)
    {
        String data = context.getDatabasePath("danhsach.db").getPath();
        SQLiteDatabase sql = openDatabase(data, null, CREATE_IF_NECESSARY);
        Cursor cursor = sql.query("ListPhoneBlock",null, "prefixNum=?", new String[]{phoneNumber}, null, null, null, null);
        cursor.moveToFirst();
//        cursor.close();
        return cursor.isAfterLast();
    }

    //Xóa tin nhắn khi số điện thoại nằm trong danh sách bị chặn
    public void XoaSms(Context context,  String phoneNumber)
    {
        try {
            if(!checkDumpNumber(context, phoneNumber))
            {
                context.getContentResolver().delete(Uri.parse("content://sms"), "address=?", new String[]{address});
                Toast.makeText(context, "Đã xóa tin nhắn từ số " + phoneNumber, Toast.LENGTH_SHORT).show();
                MySmsReceive.this.abortBroadcast();
            }
            else
            {
                String body = sms.getMessageBody();
                message +="SMS từ số "+ address +": \n" + body;
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            // TODO: handle exception
            Toast.makeText(context, "Không xóa được tin nhắn" + e, Toast.LENGTH_SHORT).show();
        }
    }

    public void XoaSmsMoiNhat(Context con, SmsMessage[]msgs)
    {
        try {
            for (int i = 0; i < msgs.length; i++) {
                if(!msgs[i].getOriginatingAddress().equals(address))
                {
                    continue;
                }
                con.getContentResolver().delete(Uri.parse("content://sms"), "address=? and date=?",
                        new String []{address,String.valueOf(msgs[i].getTimestampMillis())});
                Toast.makeText(con, "Đã xóa tin nhắn "+msgs[i].getOriginatingAddress(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            // TODO: handle exception
            Toast.makeText(con, "Không xóa được tin nhắn "+ e, Toast.LENGTH_SHORT).show();
        }
    }


}
