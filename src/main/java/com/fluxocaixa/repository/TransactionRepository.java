package com.fluxocaixa.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fluxocaixa.entity.Transaction;
import com.fluxocaixa.entity.TransactionType;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    // ── Paginação com filtros opcionais ──
    @Query("""
            SELECT t FROM Transaction t
            WHERE t.userId = :userId
              AND (:type IS NULL OR t.tipo = :type)
              AND (CAST(:dateFrom AS timestamp) IS NULL OR t.dataHora >= :dateFrom)
              AND (CAST(:dateTo AS timestamp) IS NULL OR t.dataHora <= :dateTo)
            """)
    Page<Transaction> findAllFiltered(
            @Param("userId") String userId,
            @Param("type") TransactionType type,
            @Param("dateFrom") Instant dateFrom,
            @Param("dateTo") Instant dateTo,
            Pageable pageable);

    // ── Somas para o resumo ──
    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transaction t WHERE t.userId = :userId AND t.tipo = :type")
    BigDecimal sumByUserIdAndType(@Param("userId") String userId, @Param("type") TransactionType type);

    long countByUserId(String userId);

    Optional<Transaction> findByIdAndUserId(UUID id, String userId);
}
