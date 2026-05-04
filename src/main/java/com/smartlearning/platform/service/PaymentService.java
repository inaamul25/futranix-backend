package com.smartlearning.platform.service;

import com.smartlearning.platform.dto.common.ApiResponse;
import com.smartlearning.platform.dto.payment.PaymentCallbackRequest;
import com.smartlearning.platform.dto.payment.PaymentInitResponse;

public interface PaymentService {
    PaymentInitResponse initiatePayment(Long courseId, String userEmail);
    ApiResponse handleCallback(PaymentCallbackRequest request);
    ApiResponse confirmMockPayment(String orderId, String userEmail);
}
