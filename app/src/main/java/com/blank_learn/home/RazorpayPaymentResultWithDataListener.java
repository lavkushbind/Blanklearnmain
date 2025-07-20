package com.blank_learn.home;

import com.razorpay.PaymentData;

public interface RazorpayPaymentResultWithDataListener {
    void onPaymentSuccess(String razorpayPaymentID, PaymentData paymentData);

    void onPaymentError(int code, String response, PaymentData paymentData);
}
