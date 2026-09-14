package com.example.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AzamPayRequest {

    @JsonProperty("accountNumber")
    private String accountNumber;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("externalId")
    private String externalId;

    @JsonProperty("provider")
    private String provider;

    @JsonProperty("callbackUrl")
    private String callbackUrl;
}