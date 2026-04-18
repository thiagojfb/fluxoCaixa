package com.fluxocaixa.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TransacaoRequisicaoDTO(
        @NotNull(message = "O tipo é obrigatório (CREDIT ou DEBIT_PIX)")
        String tipo,

        @NotNull(message = "O valor é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
        BigDecimal valor,

        String descricao,

        @Min(value = 1, message = "A quantidade de vezes deve ser maior ou igual a 1")
        Integer quantidadeVezes
) {
}
