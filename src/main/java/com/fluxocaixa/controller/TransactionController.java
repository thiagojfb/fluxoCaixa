package com.fluxocaixa.controller;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fluxocaixa.dto.TransactionRequest;
import com.fluxocaixa.dto.TransactionResponse;
import com.fluxocaixa.service.TransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Gerenciamento de transações financeiras")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Criar transação", description = "Registra uma nova transação. Não bloqueia se ultrapassar o salário.")
    @ApiResponse(responseCode = "201", description = "Transação criada com sucesso")
    public ResponseEntity<TransactionResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.create(getUserId(jwt), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar transações", description = "Lista transações com paginação e filtros opcionais por tipo e período.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public ResponseEntity<Page<TransactionResponse>> list(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Filtro por tipo: CREDIT ou DEBIT_PIX")
            @RequestParam(required = false) String type,
            @Parameter(description = "Data/hora início (ISO-8601, ex: 2026-01-01T00:00:00Z)")
            @RequestParam(required = false) Instant dateFrom,
            @Parameter(description = "Data/hora fim (ISO-8601, ex: 2026-12-31T23:59:59Z)")
            @RequestParam(required = false) Instant dateTo,
            @PageableDefault(size = 20, sort = "dataHora", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(
                transactionService.list(getUserId(jwt), type, dateFrom, dateTo, pageable));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover transação", description = "Remove uma transação do usuário logado.")
    @ApiResponse(responseCode = "204", description = "Transação removida com sucesso")
    @ApiResponse(responseCode = "404", description = "Transação não encontrada")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {
        transactionService.delete(getUserId(jwt), id);
        return ResponseEntity.noContent().build();
    }

    private String getUserId(Jwt jwt) {
        return jwt.getSubject();
    }
}
