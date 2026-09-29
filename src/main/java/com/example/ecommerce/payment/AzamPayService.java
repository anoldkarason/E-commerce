package com.example.ecommerce.payment;

import com.example.ecommerce.config.AzamPayConfig;
import com.example.ecommerce.dto.AzamPayRequest;
import com.example.ecommerce.dto.AzamPayResponse;
import com.example.ecommerce.exception.PaymentException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AzamPayService {

    private final AzamPayConfig config;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String cachedToken;
    private long tokenExpiry = 0;

    public synchronized String getAccessToken() {
        // Reuse cached token if still valid (with 60s safety margin)
        if (cachedToken != null && System.currentTimeMillis() < tokenExpiry - 60_000) {
            return cachedToken;
        }

        // Fail fast with a clear message if config is missing
        if (config.getAuthUrl() == null || config.getAuthUrl().isBlank()) {
            throw new PaymentException(
                    "AzamPay auth URL is not configured (authUrl is null). " +
                            "Check AZAMPAY_AUTH_URL and AzamPayConfig.authUrl.");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = new HashMap<>();
            body.put("appName", config.getAppName());
            body.put("clientId", config.getClientId());
            body.put("clientSecret", config.getClientSecret());

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            log.info("Requesting AzamPay token from {}", config.getAuthUrl());

            ResponseEntity<Map> response = restTemplate.exchange(
                    config.getAuthUrl(),            // ✅ FIXED: was getTokenUrl()
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                if (data != null && data.get("accessToken") != null) {
                    cachedToken = data.get("accessToken").toString();
                    tokenExpiry = System.currentTimeMillis() + 3600_000L; // 1 hour
                    log.info("AzamPay token acquired successfully");
                    return cachedToken;
                }
            }
            throw new PaymentException("Failed to obtain AzamPay access token");
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("AzamPay token error", e);
            throw new PaymentException("AzamPay authentication failed: " + e.getMessage(), e);
        }
    }

    public AzamPayResponse initiatePayment(AzamPayRequest request) {
        if (config.getCheckoutUrl() == null || config.getCheckoutUrl().isBlank()) {
            throw new PaymentException(
                    "AzamPay checkout URL is not configured (checkoutUrl is null). " +
                            "Check AZAMPAY_CHECKOUT_URL and AzamPayConfig.checkoutUrl.");
        }

        try {
            String token = getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            HttpEntity<AzamPayRequest> entity = new HttpEntity<>(request, headers);

            log.info("Sending AzamPay payment request to {}", config.getCheckoutUrl());

            ResponseEntity<Map> response = restTemplate.exchange(
                    config.getCheckoutUrl(),        // ✅ FIXED: was getAuthUrl()
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            log.info("AzamPay response: {}", response.getBody());

            AzamPayResponse azamResponse = new AzamPayResponse();
            if (response.getBody() != null) {
                azamResponse.setSuccess((Boolean) response.getBody().get("success"));
                azamResponse.setMessage((String) response.getBody().get("message"));
                azamResponse.setTransactionId((String) response.getBody().get("transactionId"));
                azamResponse.setReferenceId((String) response.getBody().get("referenceId"));
                azamResponse.setData(response.getBody().get("data"));
            }

            return azamResponse;
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("AzamPay payment error", e);
            throw new PaymentException("AzamPay payment failed: " + e.getMessage(), e);
        }
    }
}