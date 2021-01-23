package com.throwntech.staysafe;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class MyReceiver extends BroadcastReceiver {

    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String TAG = "SmsBroadcastReceiver";

    String msg, phoneNo;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onReceive(Context context, Intent intent) {

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("data");

        Log.i(TAG, "Intent Received: " + intent.getAction());
        if (Objects.equals(intent.getAction(), SMS_RECEIVED)) {
            Bundle dataBundle = intent.getExtras();

            if(dataBundle != null) {
                Object[] mypdu = (Object[]) dataBundle.get("pdus");
                assert mypdu != null;
                final SmsMessage[] message = new SmsMessage[mypdu.length];

                for (int i = 0; i < mypdu.length; i++) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        String format = dataBundle.getString("format");
                        message[i] = SmsMessage.createFromPdu((byte[]) mypdu[i], format);
                    } else {
                        message[i] = SmsMessage.createFromPdu((byte[]) mypdu[i]);
                    }
                    msg = message[i].getMessageBody();
                    phoneNo = message[i].getOriginatingAddress();
                }

                //For Camera ID:1
//                if (msg.equals("C1 Leopard Detected")) {
                    if (phoneNo.equals("+9913383342")) {
                        Toast.makeText(context, "Message: " + msg + "\n Number: " + phoneNo, Toast.LENGTH_LONG).show();

                        mDatabase.child("message").setValue("1");
                    }
//                }

                //For Camera ID: 2
//                if (msg.equals("C2 Leopard Detected")) {
//                    if (phoneNo.equals("+917340239617")) {        //Add new phone number
//                        Toast.makeText(context, "Message: " + msg + "\n Number: " + phoneNo, Toast.LENGTH_LONG).show();
//
//                        mDatabase.child("a2/leopard/level").setValue("1");
//                    }
//                }
            }
        }
    }
}