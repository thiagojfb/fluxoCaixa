package com.fluxocaixa.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fluxocaixa.dto.OrcamentoRespostaDTO;
import com.fluxocaixa.dto.SalarioRequisicaoDTO;
import com.fluxocaixa.dto.ResumoRespostaDTO;
import com.fluxocaixa.entity.Orcamento;
import com.fluxocaixa.entity.TipoTransacao;
import com.fluxocaixa.mapper.OrcamentoMapper;
import com.fluxocaixa.repository.OrcamentoRepository;
import com.fluxocaixa.repository.TransacaoRepository;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class OrcamentoServiceTest {

    @Mock
    private OrcamentoRepository orcamentoRepository;
    @Mock
    private TransacaoRepository transacaoRepository;
    @Mock
    private OrcamentoMapper orcamentoMapper;

    @InjectMocks
    private OrcamentoService orcamentoService;

    private static final String USUARIO_ID = "user-123";
    private Orcamento orcamento;

    @BeforeEach
    void setUp() {
        orcamento = new Orcamento();
        orcamento.setId(UUID.randomUUID());
        orcamento.setUsuarioId(USUARIO_ID);
        orcamento.setSalario(new BigDecimal("10000.00"));
        orcamento.setCriadoEm(Instant.now());
        orcamento.setAtualizadoEm(Instant.now());
        orcamento.setVersao(0L);
    }

    @Test
    @DisplayName("Deve criar orçamento automaticamente se não existir")
    void deveCriarOrcamentoSeNaoExistir() {
        when(orcamentoRepository.buscarPorUsuarioId(USUARIO_ID)).thenReturn(Optional.empty());
        when(orcamentoRepository.save(any(Orcamento.class))).thenAnswer(inv -> Objects.requireNonNull((Orcamento) inv.getArgument(0)));
        when(orcamentoMapper.toDTO(any())).thenReturn(
                new OrcamentoRespostaDTO(orcamento.getId(), BigDecimal.ZERO, Instant.now(), Instant.now()));

        OrcamentoRespostaDTO resposta = orcamentoService.obterOuCriarOrcamento(USUARIO_ID);

        assertNotNull(resposta);
        assertEquals(BigDecimal.ZERO, resposta.salario());
        verify(orcamentoRepository).save(any(Orcamento.class));
    }

    @Test
    @DisplayName("Deve retornar orçamento existente")
    void deveRetornarOrcamentoExistente() {
        when(orcamentoRepository.buscarPorUsuarioId(USUARIO_ID)).thenReturn(Optional.of(orcamento));
        when(orcamentoMapper.toDTO(orcamento)).thenReturn(
                new OrcamentoRespostaDTO(orcamento.getId(), orcamento.getSalario(), orcamento.getCriadoEm(), orcamento.getAtualizadoEm()));

        OrcamentoRespostaDTO resposta = orcamentoService.obterOuCriarOrcamento(USUARIO_ID);

        assertNotNull(resposta);
        assertEquals(new BigDecimal("10000.00"), resposta.salario());
        verify(orcamentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar salário com sucesso")
    void deveAtualizarSalario() {
        when(orcamentoRepository.buscarPorUsuarioId(USUARIO_ID)).thenReturn(Optional.of(orcamento));
        when(orcamentoRepository.save(any(Orcamento.class))).thenAnswer(inv -> Objects.requireNonNull((Orcamento) inv.getArgument(0)));
        when(orcamentoMapper.toDTO(any())).thenAnswer(inv -> {
            Orcamento o = Objects.requireNonNull((Orcamento) inv.getArgument(0));
            return new OrcamentoRespostaDTO(o.getId(), o.getSalario(), o.getCriadoEm(), o.getAtualizadoEm());
        });

        SalarioRequisicaoDTO requisicao = new SalarioRequisicaoDTO(new BigDecimal("15000.00"));
        OrcamentoRespostaDTO resposta = orcamentoService.atualizarSalario(USUARIO_ID, requisicao);

        assertEquals(new BigDecimal("15000.00"), resposta.salario());
    }

    @Test
    @DisplayName("Deve permitir reduzir salário abaixo do gasto total")
    void devePermitirSalarioAbaixoDoGastoTotal() {
        when(orcamentoRepository.buscarPorUsuarioId(USUARIO_ID)).thenReturn(Optional.of(orcamento));
        when(orcamentoRepository.save(any(Orcamento.class))).thenAnswer(inv -> Objects.requireNonNull((Orcamento) inv.getArgument(0)));
        when(orcamentoMapper.toDTO(any())).thenAnswer(inv -> {
            Orcamento o = Objects.requireNonNull((Orcamento) inv.getArgument(0));
            return new OrcamentoRespostaDTO(o.getId(), o.getSalario(), o.getCriadoEm(), o.getAtualizadoEm());
        });

        SalarioRequisicaoDTO requisicao = new SalarioRequisicaoDTO(new BigDecimal("1000.00"));
        OrcamentoRespostaDTO resposta = orcamentoService.atualizarSalario(USUARIO_ID, requisicao);

        assertEquals(new BigDecimal("1000.00"), resposta.salario());
    }

    @Test
    @DisplayName("Deve calcular saldo total corretamente")
    void deveCalcularTotalCorretamente() {
        when(orcamentoRepository.buscarPorUsuarioId(USUARIO_ID)).thenReturn(Optional.of(orcamento));
        when(transacaoRepository.somarPorUsuarioIdETipo(USUARIO_ID, TipoTransacao.CREDIT))
                .thenReturn(new BigDecimal("3000.00"));
        when(transacaoRepository.somarPorUsuarioIdETipo(USUARIO_ID, TipoTransacao.DEBIT_PIX))
                .thenReturn(new BigDecimal("3000.00"));
        when(transacaoRepository.contarPorUsuarioId(USUARIO_ID)).thenReturn(10L);

        ResumoRespostaDTO resumo = orcamentoService.obterResumo(USUARIO_ID);

        assertEquals(new BigDecimal("10000.00"), resumo.salario());
        assertEquals(new BigDecimal("3000.00"), resumo.totalGastoCredito());
        assertEquals(new BigDecimal("3000.00"), resumo.totalGastoDebitoPix());
        assertEquals(new BigDecimal("6000.00"), resumo.totalGasto());
        assertEquals(new BigDecimal("4000.00"), resumo.saldoDisponivel());
        assertEquals(10L, resumo.totalTransacoes());
    }

    @Test
    @DisplayName("Deve permitir saldo total negativo")
    void devePermitirSaldoNegativo() {
        orcamento.setSalario(new BigDecimal("5000.00"));

        when(orcamentoRepository.buscarPorUsuarioId(USUARIO_ID)).thenReturn(Optional.of(orcamento));
        when(transacaoRepository.somarPorUsuarioIdETipo(USUARIO_ID, TipoTransacao.CREDIT))
                .thenReturn(new BigDecimal("4000.00"));
        when(transacaoRepository.somarPorUsuarioIdETipo(USUARIO_ID, TipoTransacao.DEBIT_PIX))
                .thenReturn(new BigDecimal("3000.00"));
        when(transacaoRepository.contarPorUsuarioId(USUARIO_ID)).thenReturn(5L);

        ResumoRespostaDTO resumo = orcamentoService.obterResumo(USUARIO_ID);

        assertEquals(new BigDecimal("5000.00"), resumo.salario());
        assertEquals(new BigDecimal("7000.00"), resumo.totalGasto());
        assertEquals(new BigDecimal("-2000.00"), resumo.saldoDisponivel());
    }
}
