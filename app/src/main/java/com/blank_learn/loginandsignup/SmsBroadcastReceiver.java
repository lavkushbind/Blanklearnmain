package com.blank_learn.loginandsignup;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

public class SmsBroadcastReceiver extends BroadcastReceiver {

    public OtpReceiverListener otpReceiverListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);

            switch (status.getStatusCode()) {
                case CommonStatusCodes.SUCCESS:
                    // Get SMS message contents
                    String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                    if (otpReceiverListener != null) {
                        otpReceiverListener.onOtpReceived(message);
                    }
                    break;
                case CommonStatusCodes.TIMEOUT:
                    if (otpReceiverListener != null) {
                        otpReceiverListener.onOtpTimeout();
                    }
                    break;
            }
        }
    }

    public interface OtpReceiverListener {
        void onOtpReceived(String otpMessage);
        void onOtpTimeout();
    }
}
