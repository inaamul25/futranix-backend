package com.smartlearning.platform.dto.payment;

import java.util.Map;

public record PaymentCallbackRequest(
        String orderId,
        String txnId,
        String status,
        String checksumHash,
        Map<String, String> payload,
        String rawPayload
) {
}
