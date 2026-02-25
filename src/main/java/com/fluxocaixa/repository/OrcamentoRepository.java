package com.fluxocaixa.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fluxocaixa.entity.Orcamento;

@Repository
public interface OrcamentoRepository extends JpaRepository<Orcamento, UUID> {

    @Query("SELECT o FROM Orcamento o WHERE o.usuarioId = :usuarioId")
    Optional<Orcamento> buscarPorUsuarioId(@Param("usuarioId") String usuarioId);
}
