package com.fluxocaixa.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BudgetResponse(
        UUID id,
        BigDecimal salario,
        Instant criadoEm,
        Instant atualizadoEm
) {
}
