package com.smartlearning.platform.dto.payment;

public record PaymentInitResponse(
        String orderId,
        String txnToken,
        String paytmMid,
        String amount,
        String checkoutJsUrl,
        boolean sandboxMode
) {
}
