package com.fluxocaixa.service;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fluxocaixa.dto.TransacaoRequisicaoDTO;
import com.fluxocaixa.dto.TransacaoRespostaDTO;
import com.fluxocaixa.entity.Transacao;
import com.fluxocaixa.entity.TipoTransacao;
import com.fluxocaixa.mapper.TransacaoMapper;
import com.fluxocaixa.repository.TransacaoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final TransacaoMapper transacaoMapper;

    /**
     * Cria uma nova transação. Não bloqueia se ultrapassar o salário.
     */
    @Transactional
    public TransacaoRespostaDTO criar(String usuarioId, TransacaoRequisicaoDTO requisicao) {
        TipoTransacao tipo = converterTipo(requisicao.tipo());

        Transacao transacao = new Transacao();
        transacao.setUsuarioId(usuarioId);
        transacao.setTipo(tipo);
        transacao.setValor(requisicao.valor());
        transacao.setDescricao(requisicao.descricao());
        transacao.setDataHora(Instant.now());

        transacao = transacaoRepository.save(transacao);
        return transacaoMapper.toDTO(transacao);
    }

    /**
     * Lista transações com paginação e filtros opcionais.
     */
    @Transactional(readOnly = true)
    public Page<TransacaoRespostaDTO> listar(String usuarioId,
                                              String tipo,
                                              Instant dataInicio,
                                              Instant dataFim,
                                              Pageable pageable) {
        TipoTransacao tipoTransacao = null;
        if (tipo != null && !tipo.isBlank()) {
            tipoTransacao = converterTipo(tipo);
        }

        return transacaoRepository
                .buscarTodasFiltradas(usuarioId, tipoTransacao, dataInicio, dataFim, pageable)
                .map(transacaoMapper::toDTO);
    }

    /**
     * Remove uma transação do usuário logado.
     */
    @Transactional
    public void remover(String usuarioId, UUID transacaoId) {
        Transacao transacao = transacaoRepository.buscarPorIdEUsuarioId(transacaoId, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transação não encontrada: " + transacaoId));
        transacaoRepository.delete(Objects.requireNonNull(transacao));
    }

    // ── Helper ──
    private TipoTransacao converterTipo(String tipo) {
        try {
            return TipoTransacao.valueOf(tipo.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Tipo de transação inválido: '" + tipo + "'. Use CREDIT ou DEBIT_PIX");
        }
    }
}
