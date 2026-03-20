package com.fluxocaixa.controller;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fluxocaixa.dto.HistoricoTransacaoRespostaDTO;
import com.fluxocaixa.service.TransacaoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/historico-transacoes")
@Tag(name = "Histórico de Transações", description = "Consulta do histórico das transações fechadas em fatura")
@RequiredArgsConstructor
public class HistoricoTransacaoController {

    private final TransacaoService transacaoService;

    @GetMapping
    @Operation(summary = "Listar histórico de transações", description = "Lista transações transportadas para o histórico no fechamento da fatura.")
    @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso")
    public ResponseEntity<Page<HistoricoTransacaoRespostaDTO>> listarHistorico(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Data/hora início (ISO-8601, ex: 2026-01-01T00:00:00Z)")
            @RequestParam(required = false) Instant dataInicio,
            @Parameter(description = "Data/hora fim (ISO-8601, ex: 2026-12-31T23:59:59Z)")
            @RequestParam(required = false) Instant dataFim,
            @PageableDefault(size = 20, sort = "fechadoEm", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(transacaoService.listarHistorico(obterUsuarioId(jwt), dataInicio, dataFim, pageable));
    }

    private String obterUsuarioId(Jwt jwt) {
        String subject = jwt.getSubject();
        if (subject != null && !subject.isBlank()) {
            return subject;
        }
        String username = jwt.getClaimAsString("preferred_username");
        if (username != null && !username.isBlank()) {
            return username;
        }
        throw new IllegalStateException("Token JWT não contém identificação do usuário (sub ou preferred_username)");
    }
}
