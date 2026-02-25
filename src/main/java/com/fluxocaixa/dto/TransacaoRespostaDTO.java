package com.fluxocaixa.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransacaoRespostaDTO(
        UUID id,
        String tipo,
        String descricao,
        BigDecimal valor,
        Instant dataHora,
        Instant criadoEm
) {
}
