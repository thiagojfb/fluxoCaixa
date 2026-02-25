package com.fluxocaixa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fluxocaixa.dto.BudgetResponse;
import com.fluxocaixa.dto.SalaryRequest;
import com.fluxocaixa.dto.SummaryResponse;
import com.fluxocaixa.service.BudgetService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@Tag(name = "Budget", description = "Gerenciamento do orçamento mensal")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping("/budget")
    @Operation(summary = "Obter orçamento", description = "Retorna o orçamento do usuário logado. Cria automaticamente se não existir.")
    @ApiResponse(responseCode = "200", description = "Orçamento retornado com sucesso")
    public ResponseEntity<BudgetResponse> getBudget(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(budgetService.getOrCreateBudget(getUserId(jwt)));
    }

    @PutMapping("/budget/salary")
    @Operation(summary = "Atualizar salário", description = "Atualiza o salário. É permitido reduzir abaixo do total gasto.")
    @ApiResponse(responseCode = "200", description = "Salário atualizado com sucesso")
    public ResponseEntity<BudgetResponse> updateSalary(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody SalaryRequest request) {
        return ResponseEntity.ok(budgetService.updateSalary(getUserId(jwt), request));
    }

    @GetMapping("/summary")
    @Operation(summary = "Resumo financeiro", description = "Retorna salary, totalSpentCredit, totalSpentDebitPix, totalSpent, availableTotal e transactionCount")
    @ApiResponse(responseCode = "200", description = "Resumo retornado com sucesso")
    public ResponseEntity<SummaryResponse> getSummary(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(budgetService.getSummary(getUserId(jwt)));
    }

    private String getUserId(Jwt jwt) {
        return jwt.getSubject();
    }
}
