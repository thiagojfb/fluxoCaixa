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

import com.fluxocaixa.entity.Transacao;
import com.fluxocaixa.entity.TipoTransacao;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, UUID> {

    // ── Paginação com filtros opcionais ──
    @Query("""
            SELECT t FROM Transacao t
            WHERE t.usuarioId = :usuarioId
              AND (:tipo IS NULL OR t.tipo = :tipo)
              AND (CAST(:dataInicio AS timestamp) IS NULL OR t.dataHora >= :dataInicio)
              AND (CAST(:dataFim AS timestamp) IS NULL OR t.dataHora <= :dataFim)
            """)
    Page<Transacao> buscarTodasFiltradas(
            @Param("usuarioId") String usuarioId,
            @Param("tipo") TipoTransacao tipo,
            @Param("dataInicio") Instant dataInicio,
            @Param("dataFim") Instant dataFim,
            Pageable pageable);

    // ── Somas para o resumo ──
    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t WHERE t.usuarioId = :usuarioId AND t.tipo = :tipo")
    BigDecimal somarPorUsuarioIdETipo(@Param("usuarioId") String usuarioId, @Param("tipo") TipoTransacao tipo);

    @Query("SELECT COUNT(t) FROM Transacao t WHERE t.usuarioId = :usuarioId")
    long contarPorUsuarioId(@Param("usuarioId") String usuarioId);

    @Query("SELECT t FROM Transacao t WHERE t.id = :id AND t.usuarioId = :usuarioId")
    Optional<Transacao> buscarPorIdEUsuarioId(@Param("id") UUID id, @Param("usuarioId") String usuarioId);
}
