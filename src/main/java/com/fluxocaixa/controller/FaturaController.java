package com.fluxocaixa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fluxocaixa.dto.FechamentoFaturaRespostaDTO;
import com.fluxocaixa.service.TransacaoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/fatura")
@Tag(name = "Fatura", description = "Operações de fechamento da fatura de cartão de crédito")
@RequiredArgsConstructor
public class FaturaController {

    private final TransacaoService transacaoService;

    @PostMapping("/fechar")
    @Operation(summary = "Fechar fatura", description = "Transporta as transações de crédito para o histórico e zera as transações atuais do usuário.")
    @ApiResponse(responseCode = "200", description = "Fatura fechada com sucesso")
    public ResponseEntity<FechamentoFaturaRespostaDTO> fecharFatura(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(transacaoService.fecharFatura(obterUsuarioId(jwt)));
    }

    @PostMapping("/fechar-debito-pix")
    @Operation(summary = "Fechar saldo débito/PIX", description = "Transporta as transações de débito/PIX para o histórico e zera as transações de débito/PIX atuais do usuário.")
    @ApiResponse(responseCode = "200", description = "Saldo débito/PIX fechado com sucesso")
    public ResponseEntity<FechamentoFaturaRespostaDTO> fecharSaldoDebitoPix(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(transacaoService.fecharSaldoDebitoPix(obterUsuarioId(jwt)));
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
