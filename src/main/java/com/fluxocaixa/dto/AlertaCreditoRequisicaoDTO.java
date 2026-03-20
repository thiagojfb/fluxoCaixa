package com.fluxocaixa.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record AlertaCreditoRequisicaoDTO(
        @NotNull(message = "O valor de alerta é obrigatório")
        @DecimalMin(value = "0.00", message = "O valor de alerta não pode ser negativo")
        BigDecimal alertaCredito
) {
}
