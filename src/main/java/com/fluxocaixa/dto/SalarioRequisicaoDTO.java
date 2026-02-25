package com.fluxocaixa.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record SalarioRequisicaoDTO(
        @NotNull(message = "O salário é obrigatório")
        @DecimalMin(value = "0.00", message = "O salário não pode ser negativo")
        BigDecimal salario
) {
}
