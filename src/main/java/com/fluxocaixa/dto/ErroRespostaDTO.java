package com.fluxocaixa.dto;

import java.time.Instant;

public record ErroRespostaDTO(
        Instant momento,
        int status,
        String erro,
        String mensagem,
        String caminho
) {
}
