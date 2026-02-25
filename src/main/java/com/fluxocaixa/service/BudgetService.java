package com.fluxocaixa.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fluxocaixa.dto.BudgetResponse;
import com.fluxocaixa.dto.SalaryRequest;
import com.fluxocaixa.dto.SummaryResponse;
import com.fluxocaixa.entity.Budget;
import com.fluxocaixa.entity.TransactionType;
import com.fluxocaixa.mapper.BudgetMapper;
import com.fluxocaixa.repository.BudgetRepository;
import com.fluxocaixa.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetMapper budgetMapper;

    /**
     * Retorna o orçamento do usuário. Se não existir, cria com salário 0.
     */
    @Transactional
    public BudgetResponse getOrCreateBudget(String userId) {
        Budget budget = findOrCreate(userId);
        return budgetMapper.toResponse(budget);
    }

    /**
     * Atualiza o salário. Permite qualquer valor >= 0, mesmo abaixo do gasto total.
     */
    @Transactional
    public BudgetResponse updateSalary(String userId, SalaryRequest request) {
        Budget budget = findOrCreate(userId);
        budget.setSalario(request.salary());
        budget = budgetRepository.save(budget);
        return budgetMapper.toResponse(budget);
    }

    /**
     * Retorna o resumo financeiro do usuário.
     */
    @Transactional(readOnly = true)
    public SummaryResponse getSummary(String userId) {
        Budget budget = findOrCreate(userId);

        BigDecimal totalCredit = transactionRepository
                .sumByUserIdAndType(userId, TransactionType.CREDIT);
        BigDecimal totalDebitPix = transactionRepository
                .sumByUserIdAndType(userId, TransactionType.DEBIT_PIX);
        BigDecimal totalSpent = totalCredit.add(totalDebitPix);
        BigDecimal availableTotal = budget.getSalario().subtract(totalSpent);
        long count = transactionRepository.countByUserId(userId);

        return new SummaryResponse(
                budget.getSalario(),
                totalCredit,
                totalDebitPix,
                totalSpent,
                availableTotal,
                count
        );
    }

    // ── Helper ──
    Budget findOrCreate(String userId) {
        return budgetRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Budget b = new Budget();
                    b.setUserId(userId);
                    b.setSalario(BigDecimal.ZERO);
                    return budgetRepository.save(b);
                });
    }
}
