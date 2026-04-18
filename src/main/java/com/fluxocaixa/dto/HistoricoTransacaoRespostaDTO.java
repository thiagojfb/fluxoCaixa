package com.fluxocaixa.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record HistoricoTransacaoRespostaDTO(
        UUID id,
        String tipo,
        String descricao,
        BigDecimal valor,
        Integer quantidadeVezes,
        Instant dataHora,
        Instant criadoEm,
        Instant fechadoEm
) {
}
