package com.fluxocaixa.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fluxocaixa.dto.FechamentoFaturaRespostaDTO;
import com.fluxocaixa.dto.TransacaoRequisicaoDTO;
import com.fluxocaixa.dto.TransacaoRespostaDTO;
import com.fluxocaixa.entity.Transacao;
import com.fluxocaixa.mapper.HistoricoTransacaoMapper;
import com.fluxocaixa.mapper.TransacaoMapper;
import com.fluxocaixa.repository.HistoricoTransacaoRepository;
import com.fluxocaixa.repository.TransacaoRepository;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class TransacaoServiceTest {

    @Mock
    private TransacaoRepository transacaoRepository;
    @Mock
    private HistoricoTransacaoRepository historicoTransacaoRepository;
    @Mock
    private TransacaoMapper transacaoMapper;
    @Mock
    private HistoricoTransacaoMapper historicoTransacaoMapper;

    @InjectMocks
    private TransacaoService transacaoService;

    private static final String USUARIO_ID = "user-123";

    @Test
    @DisplayName("Deve criar transação CREDIT com sucesso")
    void deveCriarTransacaoCreditoComSucesso() {
        TransacaoRequisicaoDTO requisicao = new TransacaoRequisicaoDTO("CREDIT", new BigDecimal("100.00"), "Teste crédito");

        when(transacaoRepository.save(any(Transacao.class))).thenAnswer(inv -> {
            Transacao t = inv.getArgument(0);
            t.setId(UUID.randomUUID());
            t.setCriadoEm(Instant.now());
            return t;
        });
        when(transacaoMapper.toDTO(any())).thenAnswer(inv -> {
            Transacao t = inv.getArgument(0);
            return new TransacaoRespostaDTO(t.getId(), t.getTipo().name(), t.getDescricao(),
                    t.getValor(), t.getDataHora(), t.getCriadoEm());
        });

        TransacaoRespostaDTO resposta = transacaoService.criar(USUARIO_ID, requisicao);

        assertNotNull(resposta);
        assertEquals("CREDIT", resposta.tipo());
        assertEquals(new BigDecimal("100.00"), resposta.valor());
        verify(transacaoRepository).save(any(Transacao.class));
    }

    @Test
    @DisplayName("Deve criar transação DEBIT_PIX com sucesso")
    void deveCriarTransacaoDebitoPixComSucesso() {
        TransacaoRequisicaoDTO requisicao = new TransacaoRequisicaoDTO("DEBIT_PIX", new BigDecimal("250.00"), "PIX");

        when(transacaoRepository.save(any(Transacao.class))).thenAnswer(inv -> {
            Transacao t = inv.getArgument(0);
            t.setId(UUID.randomUUID());
            t.setCriadoEm(Instant.now());
            return t;
        });
        when(transacaoMapper.toDTO(any())).thenAnswer(inv -> {
            Transacao t = inv.getArgument(0);
            return new TransacaoRespostaDTO(t.getId(), t.getTipo().name(), t.getDescricao(),
                    t.getValor(), t.getDataHora(), t.getCriadoEm());
        });

        TransacaoRespostaDTO resposta = transacaoService.criar(USUARIO_ID, requisicao);

        assertNotNull(resposta);
        assertEquals("DEBIT_PIX", resposta.tipo());
        assertEquals(new BigDecimal("250.00"), resposta.valor());
    }

    @Test
    @DisplayName("Deve rejeitar tipo de transação inválido")
    void deveRejeitarTipoInvalido() {
        TransacaoRequisicaoDTO requisicao = new TransacaoRequisicaoDTO("INVALID", new BigDecimal("100.00"), null);

        assertThrows(IllegalArgumentException.class,
                () -> transacaoService.criar(USUARIO_ID, requisicao));
    }

    @Test
    @DisplayName("Deve permitir transação que ultrapasse o salário (sem bloqueio)")
    void devePermitirTransacaoExcedendoSalario() {
        TransacaoRequisicaoDTO requisicao = new TransacaoRequisicaoDTO("CREDIT", new BigDecimal("999999.00"), "Grande compra");

        when(transacaoRepository.save(any(Transacao.class))).thenAnswer(inv -> {
            Transacao t = inv.getArgument(0);
            t.setId(UUID.randomUUID());
            t.setCriadoEm(Instant.now());
            return t;
        });
        when(transacaoMapper.toDTO(any())).thenAnswer(inv -> {
            Transacao t = inv.getArgument(0);
            return new TransacaoRespostaDTO(t.getId(), t.getTipo().name(), t.getDescricao(),
                    t.getValor(), t.getDataHora(), t.getCriadoEm());
        });

        TransacaoRespostaDTO resposta = transacaoService.criar(USUARIO_ID, requisicao);

        assertNotNull(resposta);
        assertEquals(new BigDecimal("999999.00"), resposta.valor());
    }

    @Test
    @DisplayName("Deve remover transação do usuário logado")
    void deveRemoverTransacao() {
        UUID transacaoId = UUID.randomUUID();
        Transacao transacao = new Transacao();
        transacao.setId(transacaoId);
        transacao.setUsuarioId(USUARIO_ID);

        when(transacaoRepository.buscarPorIdEUsuarioId(transacaoId, USUARIO_ID)).thenReturn(Optional.of(transacao));

        transacaoService.remover(USUARIO_ID, transacaoId);

        verify(transacaoRepository).delete(transacao);
    }

    @Test
    @DisplayName("Deve lançar exceção ao remover transação inexistente")
    void deveLancarExcecaoAoRemoverInexistente() {
        UUID transacaoId = UUID.randomUUID();
        when(transacaoRepository.buscarPorIdEUsuarioId(transacaoId, USUARIO_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> transacaoService.remover(USUARIO_ID, transacaoId));
    }

    @Test
    @DisplayName("Deve fechar fatura transportando créditos para histórico e limpando transações atuais")
    void deveFecharFatura() {
        Transacao credito1 = new Transacao();
        credito1.setId(UUID.randomUUID());
        credito1.setUsuarioId(USUARIO_ID);
        credito1.setValor(new BigDecimal("120.00"));

        Transacao credito2 = new Transacao();
        credito2.setId(UUID.randomUUID());
        credito2.setUsuarioId(USUARIO_ID);
        credito2.setValor(new BigDecimal("80.00"));

        when(transacaoRepository.findByUsuarioIdAndTipo(USUARIO_ID, com.fluxocaixa.entity.TipoTransacao.CREDIT))
                .thenReturn(List.of(credito1, credito2));
        when(transacaoRepository.deleteByUsuarioIdAndTipo(USUARIO_ID, com.fluxocaixa.entity.TipoTransacao.CREDIT))
            .thenReturn(2L);
        when(historicoTransacaoRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        FechamentoFaturaRespostaDTO resposta = transacaoService.fecharFatura(USUARIO_ID);

        assertNotNull(resposta.fechadoEm());
        assertEquals(2L, resposta.quantidadeTransacoesCreditoTransportadas());
        assertEquals(new BigDecimal("200.00"), resposta.totalFaturaFechada());
        assertEquals(2L, resposta.quantidadeTransacoesRemovidas());
        verify(historicoTransacaoRepository).saveAll(any());
        verify(transacaoRepository)
            .deleteByUsuarioIdAndTipo(USUARIO_ID, com.fluxocaixa.entity.TipoTransacao.CREDIT);
    }

        @Test
        @DisplayName("Deve fechar saldo débito/PIX transportando débito/PIX para histórico")
        void deveFecharSaldoDebitoPix() {
        Transacao debito1 = new Transacao();
        debito1.setId(UUID.randomUUID());
        debito1.setUsuarioId(USUARIO_ID);
        debito1.setValor(new BigDecimal("70.00"));

        Transacao debito2 = new Transacao();
        debito2.setId(UUID.randomUUID());
        debito2.setUsuarioId(USUARIO_ID);
        debito2.setValor(new BigDecimal("30.00"));

        when(transacaoRepository.findByUsuarioIdAndTipo(USUARIO_ID, com.fluxocaixa.entity.TipoTransacao.DEBIT_PIX))
            .thenReturn(List.of(debito1, debito2));
        when(transacaoRepository.deleteByUsuarioIdAndTipo(USUARIO_ID, com.fluxocaixa.entity.TipoTransacao.DEBIT_PIX))
            .thenReturn(2L);
        when(historicoTransacaoRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        FechamentoFaturaRespostaDTO resposta = transacaoService.fecharSaldoDebitoPix(USUARIO_ID);

        assertNotNull(resposta.fechadoEm());
        assertEquals(2L, resposta.quantidadeTransacoesCreditoTransportadas());
        assertEquals(new BigDecimal("100.00"), resposta.totalFaturaFechada());
        assertEquals(2L, resposta.quantidadeTransacoesRemovidas());
        verify(historicoTransacaoRepository).saveAll(any());
        verify(transacaoRepository)
            .deleteByUsuarioIdAndTipo(USUARIO_ID, com.fluxocaixa.entity.TipoTransacao.DEBIT_PIX);
        }
}
