package com.fluxocaixa.dto;

import java.math.BigDecimal;

public record ResumoRespostaDTO(
        BigDecimal salario,
        BigDecimal totalGastoCredito,
        BigDecimal totalGastoDebitoPix,
        BigDecimal totalGasto,
        BigDecimal saldoDisponivel,
        long totalTransacoes
) {
}
