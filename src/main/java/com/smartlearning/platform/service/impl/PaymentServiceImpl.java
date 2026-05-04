package com.smartlearning.platform.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pg.merchant.PaytmChecksum;
import com.smartlearning.platform.config.AppProperties;
import com.smartlearning.platform.dto.common.ApiResponse;
import com.smartlearning.platform.dto.payment.PaymentCallbackRequest;
import com.smartlearning.platform.dto.payment.PaymentInitResponse;
import com.smartlearning.platform.entity.Course;
import com.smartlearning.platform.entity.Enrollment;
import com.smartlearning.platform.entity.Payment;
import com.smartlearning.platform.entity.User;
import com.smartlearning.platform.entity.enums.PaymentStatus;
import com.smartlearning.platform.exception.BadRequestException;
import com.smartlearning.platform.exception.UnauthorizedException;
import com.smartlearning.platform.repository.EnrollmentRepository;
import com.smartlearning.platform.repository.PaymentRepository;
import com.smartlearning.platform.service.EnrollmentService;
import com.smartlearning.platform.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final SupportService supportService;
    private final PaymentRepository paymentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentService enrollmentService;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.builder().build();

    @Override
    @Transactional
    public PaymentInitResponse initiatePayment(Long courseId, String userEmail) {
        User user = supportService.getUserByEmail(userEmail);
        Course course = supportService.getCourse(courseId);

        if (course.getCreator().getId().equals(user.getId())) {
            throw new BadRequestException("Course creators already have access to their own course.");
        }
        if (enrollmentService.isEnrolled(courseId, user.getId())) {
            throw new BadRequestException("You already have access to this course.");
        }
        if (isFreeCourse(course)) {
            throw new BadRequestException("This course is free. Use enroll instead of payment.");
        }

        Payment payment = new Payment();
        payment.setOrderId("ORDER-" + UUID.randomUUID());
        payment.setUser(user);
        payment.setCourse(course);
        payment.setAmount(course.getPrice());
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);

        if (!appProperties.getPaytm().isEnabled()) {
            payment.setGatewayResponse(serialize(Map.of(
                    "mode", "mock",
                    "note", "Mock payment created. Confirm it through the authenticated mock endpoint."
            )));
            return new PaymentInitResponse(
                    payment.getOrderId(),
                    "mock-txn-token-" + UUID.randomUUID(),
                    appProperties.getPaytm().getMid(),
                    course.getPrice().toPlainString(),
                    resolveCheckoutJsUrl(),
                    true
            );
        }

        ensurePaytmConfigured();

        String callbackUrl = buildCallbackUrl(courseId);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("requestType", "Payment");
        body.put("mid", appProperties.getPaytm().getMid());
        body.put("websiteName", appProperties.getPaytm().getWebsite());
        body.put("orderId", payment.getOrderId());
        body.put("callbackUrl", callbackUrl);
        body.put("txnAmount", Map.of(
                "value", course.getPrice().toPlainString(),
                "currency", "INR"
        ));
        body.put("userInfo", Map.of(
                "custId", String.valueOf(user.getId())
        ));

        String bodyJson = serialize(body);
        String signature = generateSignature(bodyJson);
        Map<String, Object> request = Map.of(
                "body", body,
                "head", Map.of("signature", signature)
        );

        JsonNode response;
        try {
            response = restClient.post()
                    .uri(appProperties.getPaytm().getInitiateUrl() + "?mid={mid}&orderId={orderId}",
                            appProperties.getPaytm().getMid(), payment.getOrderId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException ex) {
            throw new BadRequestException("Unable to connect to Paytm right now. Please try again.");
        }

        if (response == null) {
            throw new BadRequestException("Paytm did not return a usable transaction token.");
        }

        String txnToken = response.path("body").path("txnToken").asText("");
        String resultStatus = response.path("body").path("resultInfo").path("resultStatus").asText(
                response.path("body").path("resultInfo").path("resultCode").asText("")
        );

        payment.setGatewayResponse(response.toString());

        if (txnToken.isBlank()) {
            throw new BadRequestException("Paytm did not return a transaction token.");
        }
        if (!resultStatus.isBlank()
                && !"S".equalsIgnoreCase(resultStatus)
                && !"SUCCESS".equalsIgnoreCase(resultStatus)
                && !"TXN_SUCCESS".equalsIgnoreCase(resultStatus)) {
            throw new BadRequestException("Paytm could not initialize the transaction.");
        }

        return new PaymentInitResponse(
                payment.getOrderId(),
                txnToken,
                appProperties.getPaytm().getMid(),
                course.getPrice().toPlainString(),
                resolveCheckoutJsUrl(),
                false
        );
    }

    @Override
    @Transactional
    public ApiResponse handleCallback(PaymentCallbackRequest request) {
        Payment payment = paymentRepository.findByOrderId(request.orderId())
                .orElseThrow(() -> new BadRequestException("Payment order not found"));

        if (appProperties.getPaytm().isEnabled()) {
            ensurePaytmConfigured();
            verifyChecksum(request);
            JsonNode statusResponse = fetchTransactionStatus(payment.getOrderId());
            payment.setGatewayResponse(statusResponse.toString());

            String txnStatus = statusResponse.path("body").path("resultInfo").path("resultStatus").asText(
                    statusResponse.path("body").path("txnInfo").path("STATUS").asText(request.status())
            );
            String txnId = statusResponse.path("body").path("txnInfo").path("TXNID").asText(request.txnId());
            payment.setProviderTransactionId(txnId);

            if ("TXN_SUCCESS".equalsIgnoreCase(txnStatus) || "SUCCESS".equalsIgnoreCase(txnStatus)) {
                payment.setStatus(PaymentStatus.SUCCESS);
                enrollPaidCourse(payment);
                return new ApiResponse(true, "Payment successful and course unlocked");
            }

            payment.setStatus(PaymentStatus.FAILED);
            return new ApiResponse(false, "Payment failed");
        }

        payment.setProviderTransactionId(request.txnId());
        payment.setGatewayResponse(request.rawPayload());
        if ("TXN_SUCCESS".equalsIgnoreCase(request.status()) || "SUCCESS".equalsIgnoreCase(request.status())) {
            payment.setStatus(PaymentStatus.SUCCESS);
            enrollPaidCourse(payment);
            return new ApiResponse(true, "Payment successful and course unlocked");
        }
        payment.setStatus(PaymentStatus.FAILED);
        return new ApiResponse(false, "Payment failed");
    }

    @Override
    @Transactional
    public ApiResponse confirmMockPayment(String orderId, String userEmail) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BadRequestException("Payment order not found"));
        if (appProperties.getPaytm().isEnabled()) {
            throw new BadRequestException("Mock confirmation is disabled while Paytm is enabled.");
        }
        if (!payment.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new UnauthorizedException("You cannot confirm someone else's payment.");
        }
        payment.setProviderTransactionId("MOCK-" + UUID.randomUUID());
        payment.setGatewayResponse(serialize(Map.of(
                "mode", "mock",
                "status", "TXN_SUCCESS",
                "orderId", orderId
        )));
        payment.setStatus(PaymentStatus.SUCCESS);
        enrollPaidCourse(payment);
        return new ApiResponse(true, "Mock payment successful and course unlocked");
    }

    private void enrollPaidCourse(Payment payment) {
        if (!enrollmentService.isEnrolled(payment.getCourse().getId(), payment.getUser().getId())) {
            Enrollment enrollment = new Enrollment();
            enrollment.setCourse(payment.getCourse());
            enrollment.setUser(payment.getUser());
            enrollmentRepository.save(enrollment);
        }
    }

    private JsonNode fetchTransactionStatus(String orderId) {
        Map<String, Object> body = Map.of(
                "mid", appProperties.getPaytm().getMid(),
                "orderId", orderId
        );
        String bodyJson = serialize(body);
        String signature = generateSignature(bodyJson);
        Map<String, Object> request = Map.of(
                "body", body,
                "head", Map.of("signature", signature)
        );

        try {
            JsonNode response = restClient.post()
                    .uri(appProperties.getPaytm().getStatusUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(JsonNode.class);
            if (response == null) {
                throw new BadRequestException("Paytm returned an empty status response.");
            }
            return response;
        } catch (RestClientException ex) {
            throw new BadRequestException("Unable to verify Paytm payment status.");
        }
    }

    private void verifyChecksum(PaymentCallbackRequest request) {
        if (request.checksumHash() == null || request.checksumHash().isBlank()) {
            throw new BadRequestException("Missing Paytm checksum in callback.");
        }

        TreeMap<String, String> payload = new TreeMap<>(request.payload());
        payload.remove("CHECKSUMHASH");
        payload.remove("checksumhash");

        try {
            boolean valid = PaytmChecksum.verifySignature(payload, appProperties.getPaytm().getMerchantKey(), request.checksumHash());
            if (!valid) {
                throw new BadRequestException("Invalid Paytm callback checksum.");
            }
        } catch (Exception ex) {
            if (ex instanceof BadRequestException badRequestException) {
                throw badRequestException;
            }
            throw new BadRequestException("Unable to verify Paytm callback checksum.");
        }
    }

    private String generateSignature(String bodyJson) {
        try {
            return PaytmChecksum.generateSignature(bodyJson, appProperties.getPaytm().getMerchantKey());
        } catch (Exception ex) {
            throw new BadRequestException("Unable to generate Paytm checksum.");
        }
    }

    private void ensurePaytmConfigured() {
        AppProperties.Paytm paytm = appProperties.getPaytm();
        if (isBlank(paytm.getMid())
                || isBlank(paytm.getMerchantKey())
                || isBlank(paytm.getWebsite())
                || isBlank(paytm.getCallbackUrl())
                || isBlank(paytm.getInitiateUrl())
                || isBlank(paytm.getStatusUrl())) {
            throw new BadRequestException("Paytm is enabled but configuration is incomplete.");
        }
    }

    private String buildCallbackUrl(Long courseId) {
        return appProperties.getPaytm().getCallbackUrl() + "?courseId=" + courseId;
    }

    private String resolveCheckoutJsUrl() {
        String host = appProperties.getPaytm().getInitiateUrl() != null
                && appProperties.getPaytm().getInitiateUrl().contains("stage")
                ? "https://securegw-stage.paytm.in"
                : "https://securegw.paytm.in";
        return host + "/merchantpgpui/checkoutjs/merchants/" + appProperties.getPaytm().getMid() + ".js";
    }

    private boolean isFreeCourse(Course course) {
        return course.getPrice() == null || course.getPrice().compareTo(BigDecimal.ZERO) <= 0;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String serialize(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return String.valueOf(payload);
        }
    }
}
