package com.fluxocaixa.repository;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fluxocaixa.entity.HistoricoTransacao;

@Repository
public interface HistoricoTransacaoRepository extends JpaRepository<HistoricoTransacao, UUID> {

        @Query("""
                        SELECT h FROM HistoricoTransacao h
                        WHERE h.usuarioId = :usuarioId
                            AND (CAST(:dataInicio AS timestamp) IS NULL OR h.dataHora >= :dataInicio)
                            AND (CAST(:dataFim AS timestamp) IS NULL OR h.dataHora <= :dataFim)
                        ORDER BY h.fechadoEm DESC, h.dataHora DESC
                        """)
        Page<HistoricoTransacao> buscarHistoricoFiltrado(
                        @Param("usuarioId") String usuarioId,
                        @Param("dataInicio") Instant dataInicio,
                        @Param("dataFim") Instant dataFim,
                        Pageable pageable);
}
