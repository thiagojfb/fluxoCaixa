package com.fluxocaixa.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fluxocaixa.dto.OrcamentoRespostaDTO;
import com.fluxocaixa.dto.SalarioRequisicaoDTO;
import com.fluxocaixa.dto.ResumoRespostaDTO;
import com.fluxocaixa.entity.Orcamento;
import com.fluxocaixa.entity.TipoTransacao;
import com.fluxocaixa.mapper.OrcamentoMapper;
import com.fluxocaixa.repository.OrcamentoRepository;
import com.fluxocaixa.repository.TransacaoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrcamentoService {

    private final OrcamentoRepository orcamentoRepository;
    private final TransacaoRepository transacaoRepository;
    private final OrcamentoMapper orcamentoMapper;

    /**
     * Retorna o orçamento do usuário. Se não existir, cria com salário 0.
     */
    @Transactional
    public OrcamentoRespostaDTO obterOuCriarOrcamento(String usuarioId) {
        Orcamento orcamento = buscarOuCriar(usuarioId);
        return orcamentoMapper.toDTO(orcamento);
    }

    /**
     * Atualiza o salário. Permite qualquer valor >= 0, mesmo abaixo do gasto total.
     */
    @Transactional
    public OrcamentoRespostaDTO atualizarSalario(String usuarioId, SalarioRequisicaoDTO requisicao) {
        Orcamento orcamento = buscarOuCriar(usuarioId);
        orcamento.setSalario(requisicao.salario());
        orcamento = orcamentoRepository.save(orcamento);
        return orcamentoMapper.toDTO(orcamento);
    }

    /**
     * Retorna o resumo financeiro do usuário.
     */
    @Transactional(readOnly = true)
    public ResumoRespostaDTO obterResumo(String usuarioId) {
        Orcamento orcamento = buscarOuCriar(usuarioId);

        BigDecimal totalCredito = transacaoRepository
                .somarPorUsuarioIdETipo(usuarioId, TipoTransacao.CREDIT);
        BigDecimal totalDebitoPix = transacaoRepository
                .somarPorUsuarioIdETipo(usuarioId, TipoTransacao.DEBIT_PIX);
        BigDecimal totalGasto = totalCredito.add(totalDebitoPix);
        BigDecimal saldoDisponivel = orcamento.getSalario().subtract(totalGasto);
        long totalTransacoes = transacaoRepository.contarPorUsuarioId(usuarioId);

        return new ResumoRespostaDTO(
                orcamento.getSalario(),
                totalCredito,
                totalDebitoPix,
                totalGasto,
                saldoDisponivel,
                totalTransacoes
        );
    }

    // ── Helper ──
    Orcamento buscarOuCriar(String usuarioId) {
        return orcamentoRepository.buscarPorUsuarioId(usuarioId)
                .orElseGet(() -> {
                    Orcamento o = new Orcamento();
                    o.setUsuarioId(usuarioId);
                    o.setSalario(BigDecimal.ZERO);
                    return orcamentoRepository.save(o);
                });
    }
}
