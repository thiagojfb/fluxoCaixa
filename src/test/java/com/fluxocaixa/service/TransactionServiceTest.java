package com.fluxocaixa.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fluxocaixa.dto.TransactionRequest;
import com.fluxocaixa.dto.TransactionResponse;
import com.fluxocaixa.entity.Transaction;
import com.fluxocaixa.mapper.TransactionMapper;
import com.fluxocaixa.repository.TransactionRepository;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;

    private static final String USER_ID = "user-123";

    @Test
    @DisplayName("Deve criar transação CREDIT com sucesso")
    void shouldCreateCreditTransaction() {
        TransactionRequest request = new TransactionRequest("CREDIT", new BigDecimal("100.00"), "Teste crédito");

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(UUID.randomUUID());
            t.setCriadoEm(Instant.now());
            return t;
        });
        when(transactionMapper.toResponse(any())).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            return new TransactionResponse(t.getId(), t.getTipo().name(), t.getDescricao(),
                    t.getValor(), t.getDataHora(), t.getCriadoEm());
        });

        TransactionResponse response = transactionService.create(USER_ID, request);

        assertNotNull(response);
        assertEquals("CREDIT", response.type());
        assertEquals(new BigDecimal("100.00"), response.amount());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Deve criar transação DEBIT_PIX com sucesso")
    void shouldCreateDebitPixTransaction() {
        TransactionRequest request = new TransactionRequest("DEBIT_PIX", new BigDecimal("250.00"), "PIX");

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(UUID.randomUUID());
            t.setCriadoEm(Instant.now());
            return t;
        });
        when(transactionMapper.toResponse(any())).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            return new TransactionResponse(t.getId(), t.getTipo().name(), t.getDescricao(),
                    t.getValor(), t.getDataHora(), t.getCriadoEm());
        });

        TransactionResponse response = transactionService.create(USER_ID, request);

        assertNotNull(response);
        assertEquals("DEBIT_PIX", response.type());
        assertEquals(new BigDecimal("250.00"), response.amount());
    }

    @Test
    @DisplayName("Deve rejeitar tipo de transação inválido")
    void shouldRejectInvalidType() {
        TransactionRequest request = new TransactionRequest("INVALID", new BigDecimal("100.00"), null);

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.create(USER_ID, request));
    }

    @Test
    @DisplayName("Deve permitir transação que ultrapasse o salário (sem bloqueio)")
    void shouldAllowTransactionExceedingSalary() {
        // Transação de valor alto — não deve haver verificação de saldo
        TransactionRequest request = new TransactionRequest("CREDIT", new BigDecimal("999999.00"), "Grande compra");

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(UUID.randomUUID());
            t.setCriadoEm(Instant.now());
            return t;
        });
        when(transactionMapper.toResponse(any())).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            return new TransactionResponse(t.getId(), t.getTipo().name(), t.getDescricao(),
                    t.getValor(), t.getDataHora(), t.getCriadoEm());
        });

        TransactionResponse response = transactionService.create(USER_ID, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("999999.00"), response.amount());
    }

    @Test
    @DisplayName("Deve remover transação do usuário logado")
    void shouldDeleteTransaction() {
        UUID txId = UUID.randomUUID();
        Transaction tx = new Transaction();
        tx.setId(txId);
        tx.setUserId(USER_ID);

        when(transactionRepository.findByIdAndUserId(txId, USER_ID)).thenReturn(Optional.of(tx));

        transactionService.delete(USER_ID, txId);

        verify(transactionRepository).delete(tx);
    }

    @Test
    @DisplayName("Deve lançar exceção ao remover transação inexistente")
    void shouldThrowWhenDeletingNonExistent() {
        UUID txId = UUID.randomUUID();
        when(transactionRepository.findByIdAndUserId(txId, USER_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.delete(USER_ID, txId));
    }
}
