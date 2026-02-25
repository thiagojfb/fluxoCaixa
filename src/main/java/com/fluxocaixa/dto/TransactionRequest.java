package com.fluxocaixa.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record TransactionRequest(
        @NotNull(message = "O tipo é obrigatório (CREDIT ou DEBIT_PIX)")
        String type,

        @NotNull(message = "O valor é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
        BigDecimal amount,

        String description
) {
}
