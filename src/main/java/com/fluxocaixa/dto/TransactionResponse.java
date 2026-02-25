package com.fluxocaixa.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        String type,
        String description,
        BigDecimal amount,
        Instant dateTime,
        Instant createdAt
) {
}
