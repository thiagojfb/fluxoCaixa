package com.fluxocaixa.dto;

import java.math.BigDecimal;

public record SummaryResponse(
        BigDecimal salary,
        BigDecimal totalSpentCredit,
        BigDecimal totalSpentDebitPix,
        BigDecimal totalSpent,
        BigDecimal availableTotal,
        long transactionCount
) {
}
