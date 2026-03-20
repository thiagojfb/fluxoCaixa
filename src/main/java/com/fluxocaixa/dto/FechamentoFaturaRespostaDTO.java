package com.fluxocaixa.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record FechamentoFaturaRespostaDTO(
        Instant fechadoEm,
        long quantidadeTransacoesCreditoTransportadas,
        BigDecimal totalFaturaFechada,
        long quantidadeTransacoesRemovidas
) {
}
