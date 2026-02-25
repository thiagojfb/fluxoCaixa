package com.fluxocaixa.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrcamentoRespostaDTO(
        UUID id,
        BigDecimal salario,
        Instant criadoEm,
        Instant atualizadoEm
) {
}
