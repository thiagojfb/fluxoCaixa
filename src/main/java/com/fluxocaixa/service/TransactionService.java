package com.fluxocaixa.service;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fluxocaixa.dto.TransactionRequest;
import com.fluxocaixa.dto.TransactionResponse;
import com.fluxocaixa.entity.Transaction;
import com.fluxocaixa.entity.TransactionType;
import com.fluxocaixa.mapper.TransactionMapper;
import com.fluxocaixa.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    /**
     * Cria uma nova transação. Não bloqueia se ultrapassar o salário.
     */
    @Transactional
    public TransactionResponse create(String userId, TransactionRequest request) {
        TransactionType type = parseType(request.type());

        Transaction tx = new Transaction();
        tx.setUserId(userId);
        tx.setTipo(type);
        tx.setValor(request.amount());
        tx.setDescricao(request.description());
        tx.setDataHora(Instant.now());

        tx = transactionRepository.save(tx);
        return transactionMapper.toResponse(tx);
    }

    /**
     * Lista transações com paginação e filtros opcionais.
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> list(String userId,
                                          String type,
                                          Instant dateFrom,
                                          Instant dateTo,
                                          Pageable pageable) {
        TransactionType txType = null;
        if (type != null && !type.isBlank()) {
            txType = parseType(type);
        }

        return transactionRepository
                .findAllFiltered(userId, txType, dateFrom, dateTo, pageable)
                .map(transactionMapper::toResponse);
    }

    /**
     * Remove uma transação do usuário logado.
     */
    @Transactional
    public void delete(String userId, UUID transactionId) {
        Transaction tx = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transação não encontrada: " + transactionId));
        transactionRepository.delete(Objects.requireNonNull(tx));
    }

    // ── Helper ──
    private TransactionType parseType(String type) {
        try {
            return TransactionType.valueOf(type.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Tipo de transação inválido: '" + type + "'. Use CREDIT ou DEBIT_PIX");
        }
    }
}
