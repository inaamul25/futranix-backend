package com.smartlearning.platform.controller;

import com.smartlearning.platform.dto.common.ApiResponse;
import com.smartlearning.platform.dto.payment.PaymentCallbackRequest;
import com.smartlearning.platform.dto.payment.PaymentInitResponse;
import com.smartlearning.platform.config.AppProperties;
import com.smartlearning.platform.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final AppProperties appProperties;

    @PostMapping("/create-order/{courseId}")
    public ResponseEntity<PaymentInitResponse> createOrder(@PathVariable Long courseId, Authentication authentication) {
        return ResponseEntity.ok(paymentService.initiatePayment(courseId, authentication.getName()));
    }

    @PostMapping("/mock-confirm/{orderId}")
    public ResponseEntity<ApiResponse> mockConfirm(@PathVariable String orderId, Authentication authentication) {
        return ResponseEntity.ok(paymentService.confirmMockPayment(orderId, authentication.getName()));
    }

    @PostMapping(value = "/callback", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> callback(
            @RequestParam Map<String, String> payload,
            @RequestParam(required = false) Long courseId
    ) {
        Map<String, String> safePayload = payload == null ? Map.of() : payload;
        PaymentCallbackRequest request = new PaymentCallbackRequest(
                safePayload.getOrDefault("orderId", safePayload.getOrDefault("ORDERID", "")),
                safePayload.getOrDefault("txnId", safePayload.getOrDefault("TXNID", "")),
                safePayload.getOrDefault("status", safePayload.getOrDefault("STATUS", "FAILED")),
                safePayload.getOrDefault("CHECKSUMHASH", safePayload.getOrDefault("checksumhash", "")),
                safePayload,
                safePayload.toString()
        );
        ApiResponse response = paymentService.handleCallback(request);
        String target = buildFrontendRedirect(courseId, response.success());
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, target)
                .build();
    }

    @PostMapping(value = "/callback", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> callbackJson(@RequestBody(required = false) Map<String, String> payload) {
        Map<String, String> safePayload = payload == null ? Map.of() : payload;
        PaymentCallbackRequest request = new PaymentCallbackRequest(
                safePayload.getOrDefault("orderId", safePayload.getOrDefault("ORDERID", "")),
                safePayload.getOrDefault("txnId", safePayload.getOrDefault("TXNID", "")),
                safePayload.getOrDefault("status", safePayload.getOrDefault("STATUS", "FAILED")),
                safePayload.getOrDefault("CHECKSUMHASH", safePayload.getOrDefault("checksumhash", "")),
                safePayload,
                safePayload.toString()
        );
        return ResponseEntity.ok(paymentService.handleCallback(request));
    }

    private String buildFrontendRedirect(Long courseId, boolean success) {
        String path = courseId == null ? "/" : "/courses/" + courseId;
        String separator = path.contains("?") ? "&" : "?";
        return URI.create(appProperties.getFrontendUrl() + path + separator + "payment=" + (success ? "success" : "failed")).toString();
    }
}
