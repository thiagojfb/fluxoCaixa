package com.fluxocaixa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fluxocaixa.dto.OrcamentoRespostaDTO;
import com.fluxocaixa.dto.SalarioRequisicaoDTO;
import com.fluxocaixa.dto.ResumoRespostaDTO;
import com.fluxocaixa.service.OrcamentoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@Tag(name = "Orçamento", description = "Gerenciamento do orçamento mensal")
@RequiredArgsConstructor
public class OrcamentoController {

    private final OrcamentoService orcamentoService;

    @GetMapping("/budget")
    @Operation(summary = "Obter orçamento", description = "Retorna o orçamento do usuário logado. Cria automaticamente se não existir.")
    @ApiResponse(responseCode = "200", description = "Orçamento retornado com sucesso")
    public ResponseEntity<OrcamentoRespostaDTO> obterOrcamento(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(orcamentoService.obterOuCriarOrcamento(obterUsuarioId(jwt)));
    }

    @PutMapping("/budget/salary")
    @Operation(summary = "Atualizar salário", description = "Atualiza o salário. É permitido reduzir abaixo do total gasto.")
    @ApiResponse(responseCode = "200", description = "Salário atualizado com sucesso")
    public ResponseEntity<OrcamentoRespostaDTO> atualizarSalario(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody SalarioRequisicaoDTO requisicao) {
        return ResponseEntity.ok(orcamentoService.atualizarSalario(obterUsuarioId(jwt), requisicao));
    }

    @GetMapping("/summary")
    @Operation(summary = "Resumo financeiro", description = "Retorna salário, totalGastoCredito, totalGastoDebitoPix, totalGasto, saldoDisponivel e totalTransacoes")
    @ApiResponse(responseCode = "200", description = "Resumo retornado com sucesso")
    public ResponseEntity<ResumoRespostaDTO> obterResumo(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(orcamentoService.obterResumo(obterUsuarioId(jwt)));
    }

    private String obterUsuarioId(Jwt jwt) {
        return jwt.getSubject();
    }
}
