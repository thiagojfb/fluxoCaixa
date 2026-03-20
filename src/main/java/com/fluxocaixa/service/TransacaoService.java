package com.fluxocaixa.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fluxocaixa.dto.FechamentoFaturaRespostaDTO;
import com.fluxocaixa.dto.HistoricoTransacaoRespostaDTO;
import com.fluxocaixa.dto.TransacaoRequisicaoDTO;
import com.fluxocaixa.dto.TransacaoRespostaDTO;
import com.fluxocaixa.entity.HistoricoTransacao;
import com.fluxocaixa.entity.Transacao;
import com.fluxocaixa.entity.TipoTransacao;
import com.fluxocaixa.mapper.HistoricoTransacaoMapper;
import com.fluxocaixa.mapper.TransacaoMapper;
import com.fluxocaixa.repository.HistoricoTransacaoRepository;
import com.fluxocaixa.repository.TransacaoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final HistoricoTransacaoRepository historicoTransacaoRepository;
    private final TransacaoMapper transacaoMapper;
    private final HistoricoTransacaoMapper historicoTransacaoMapper;

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
     * Atualiza uma transação do usuário logado.
     */
    @Transactional
    public TransacaoRespostaDTO atualizar(String usuarioId, UUID transacaoId, TransacaoRequisicaoDTO requisicao) {
        Transacao transacao = transacaoRepository.buscarPorIdEUsuarioId(transacaoId, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transação não encontrada: " + transacaoId));

        transacao.setTipo(converterTipo(requisicao.tipo()));
        transacao.setValor(requisicao.valor());
        transacao.setDescricao(requisicao.descricao());

        Transacao atualizada = transacaoRepository.save(transacao);
        return transacaoMapper.toDTO(atualizada);
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

    /**
     * Fecha a fatura de cartão de crédito do usuário, transportando transações de crédito
        * para o histórico e zerando apenas as transações de crédito atuais.
     */
    @Transactional
    public FechamentoFaturaRespostaDTO fecharFatura(String usuarioId) {
        return fecharSaldoPorTipo(usuarioId, TipoTransacao.CREDIT);
    }

    /**
     * Fecha o saldo de débito/PIX do usuário, transportando transações de débito/PIX
     * para o histórico e zerando apenas as transações de débito/PIX atuais.
     */
    @Transactional
    public FechamentoFaturaRespostaDTO fecharSaldoDebitoPix(String usuarioId) {
        return fecharSaldoPorTipo(usuarioId, TipoTransacao.DEBIT_PIX);
    }

    /**
     * Lista o histórico de transações transportadas no fechamento da fatura.
     */
    @Transactional(readOnly = true)
    public Page<HistoricoTransacaoRespostaDTO> listarHistorico(
            String usuarioId,
            Instant dataInicio,
            Instant dataFim,
            Pageable pageable) {
        return historicoTransacaoRepository
                .buscarHistoricoFiltrado(usuarioId, dataInicio, dataFim, pageable)
                .map(historicoTransacaoMapper::toDTO);
    }

    // ── Helper ──
    private HistoricoTransacao paraHistorico(Transacao transacao, Instant fechadoEm) {
        HistoricoTransacao historico = new HistoricoTransacao();
        historico.setUsuarioId(transacao.getUsuarioId());
        historico.setTipo(transacao.getTipo());
        historico.setDescricao(transacao.getDescricao());
        historico.setValor(transacao.getValor());
        historico.setDataHora(transacao.getDataHora());
        historico.setCriadoEm(transacao.getCriadoEm());
        historico.setFechadoEm(fechadoEm);
        return historico;
    }

        private FechamentoFaturaRespostaDTO fecharSaldoPorTipo(String usuarioId, TipoTransacao tipo) {
        Instant fechadoEm = Instant.now();

        List<Transacao> transacoes = transacaoRepository
            .findByUsuarioIdAndTipo(usuarioId, tipo);

        BigDecimal totalFechamento = transacoes.stream()
            .map(Transacao::getValor)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (!transacoes.isEmpty()) {
            List<HistoricoTransacao> historico = transacoes.stream()
                .map(t -> paraHistorico(t, fechadoEm))
                .toList();
            historicoTransacaoRepository.saveAll(Objects.requireNonNull(historico));
        }

        long quantidadeRemovidas = transacaoRepository
            .deleteByUsuarioIdAndTipo(usuarioId, tipo);

        return new FechamentoFaturaRespostaDTO(
            fechadoEm,
            transacoes.size(),
            totalFechamento,
            quantidadeRemovidas
        );
        }

    private TipoTransacao converterTipo(String tipo) {
        try {
            return TipoTransacao.valueOf(tipo.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Tipo de transação inválido: '" + tipo + "'. Use CREDIT ou DEBIT_PIX");
        }
    }
}
