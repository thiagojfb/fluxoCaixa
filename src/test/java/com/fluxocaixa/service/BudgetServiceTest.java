package com.fluxocaixa.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fluxocaixa.dto.BudgetResponse;
import com.fluxocaixa.dto.SalaryRequest;
import com.fluxocaixa.dto.SummaryResponse;
import com.fluxocaixa.entity.Budget;
import com.fluxocaixa.entity.TransactionType;
import com.fluxocaixa.mapper.BudgetMapper;
import com.fluxocaixa.repository.BudgetRepository;
import com.fluxocaixa.repository.TransactionRepository;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private BudgetMapper budgetMapper;

    @InjectMocks
    private BudgetService budgetService;

    private static final String USER_ID = "user-123";
    private Budget budget;

    @BeforeEach
    void setUp() {
        budget = new Budget();
        budget.setId(UUID.randomUUID());
        budget.setUserId(USER_ID);
        budget.setSalario(new BigDecimal("10000.00"));
        budget.setCriadoEm(Instant.now());
        budget.setAtualizadoEm(Instant.now());
        budget.setVersion(0L);
    }

    @Test
    @DisplayName("Deve criar budget automaticamente se não existir")
    void shouldCreateBudgetIfNotExists() {
        when(budgetRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> Objects.requireNonNull((Budget) inv.getArgument(0)));
        when(budgetMapper.toResponse(any())).thenReturn(
                new BudgetResponse(budget.getId(), BigDecimal.ZERO, Instant.now(), Instant.now()));

        BudgetResponse response = budgetService.getOrCreateBudget(USER_ID);

        assertNotNull(response);
        assertEquals(BigDecimal.ZERO, response.salario());
        verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    @DisplayName("Deve retornar budget existente")
    void shouldReturnExistingBudget() {
        when(budgetRepository.findByUserId(USER_ID)).thenReturn(Optional.of(budget));
        when(budgetMapper.toResponse(budget)).thenReturn(
                new BudgetResponse(budget.getId(), budget.getSalario(), budget.getCriadoEm(), budget.getAtualizadoEm()));

        BudgetResponse response = budgetService.getOrCreateBudget(USER_ID);

        assertNotNull(response);
        assertEquals(new BigDecimal("10000.00"), response.salario());
        verify(budgetRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar salário com sucesso")
    void shouldUpdateSalary() {
        when(budgetRepository.findByUserId(USER_ID)).thenReturn(Optional.of(budget));
        when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> Objects.requireNonNull((Budget) inv.getArgument(0)));
        when(budgetMapper.toResponse(any())).thenAnswer(inv -> {
            Budget b = Objects.requireNonNull((Budget) inv.getArgument(0));
            return new BudgetResponse(b.getId(), b.getSalario(), b.getCriadoEm(), b.getAtualizadoEm());
        });

        SalaryRequest request = new SalaryRequest(new BigDecimal("15000.00"));
        BudgetResponse response = budgetService.updateSalary(USER_ID, request);

        assertEquals(new BigDecimal("15000.00"), response.salario());
    }

    @Test
    @DisplayName("Deve permitir reduzir salário abaixo do gasto total")
    void shouldAllowSalaryBelowTotalSpent() {
        when(budgetRepository.findByUserId(USER_ID)).thenReturn(Optional.of(budget));
        when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> Objects.requireNonNull((Budget) inv.getArgument(0)));
        when(budgetMapper.toResponse(any())).thenAnswer(inv -> {
            Budget b = Objects.requireNonNull((Budget) inv.getArgument(0));
            return new BudgetResponse(b.getId(), b.getSalario(), b.getCriadoEm(), b.getAtualizadoEm());
        });

        // Salário menor que o gasto — deve funcionar sem erro
        SalaryRequest request = new SalaryRequest(new BigDecimal("1000.00"));
        BudgetResponse response = budgetService.updateSalary(USER_ID, request);

        assertEquals(new BigDecimal("1000.00"), response.salario());
    }

    @Test
    @DisplayName("Deve calcular saldo total corretamente")
    void shouldCalculateTotalCorrectly() {
        when(budgetRepository.findByUserId(USER_ID)).thenReturn(Optional.of(budget));
        when(transactionRepository.sumByUserIdAndType(USER_ID, TransactionType.CREDIT))
                .thenReturn(new BigDecimal("3000.00"));
        when(transactionRepository.sumByUserIdAndType(USER_ID, TransactionType.DEBIT_PIX))
                .thenReturn(new BigDecimal("3000.00"));
        when(transactionRepository.countByUserId(USER_ID)).thenReturn(10L);

        SummaryResponse summary = budgetService.getSummary(USER_ID);

        assertEquals(new BigDecimal("10000.00"), summary.salary());
        assertEquals(new BigDecimal("3000.00"), summary.totalSpentCredit());
        assertEquals(new BigDecimal("3000.00"), summary.totalSpentDebitPix());
        assertEquals(new BigDecimal("6000.00"), summary.totalSpent());
        assertEquals(new BigDecimal("4000.00"), summary.availableTotal());
        assertEquals(10L, summary.transactionCount());
    }

    @Test
    @DisplayName("Deve permitir saldo total negativo")
    void shouldAllowNegativeBalance() {
        budget.setSalario(new BigDecimal("5000.00"));

        when(budgetRepository.findByUserId(USER_ID)).thenReturn(Optional.of(budget));
        when(transactionRepository.sumByUserIdAndType(USER_ID, TransactionType.CREDIT))
                .thenReturn(new BigDecimal("4000.00"));
        when(transactionRepository.sumByUserIdAndType(USER_ID, TransactionType.DEBIT_PIX))
                .thenReturn(new BigDecimal("3000.00"));
        when(transactionRepository.countByUserId(USER_ID)).thenReturn(5L);

        SummaryResponse summary = budgetService.getSummary(USER_ID);

        assertEquals(new BigDecimal("5000.00"), summary.salary());
        assertEquals(new BigDecimal("7000.00"), summary.totalSpent());
        assertEquals(new BigDecimal("-2000.00"), summary.availableTotal());
    }
}
