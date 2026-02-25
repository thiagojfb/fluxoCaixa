package com.fluxocaixa.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orcamento", uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
@Getter
@Setter
@NoArgsConstructor
public class Orcamento {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String usuarioId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal salario = BigDecimal.ZERO;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    @Version
    @Column(name = "version")
    private Long versao;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        Instant agora = Instant.now();
        criadoEm = agora;
        atualizadoEm = agora;
    }

    @PreUpdate
    void preUpdate() {
        atualizadoEm = Instant.now();
    }
}
