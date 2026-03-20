package com.fluxocaixa.mapper;

import org.mapstruct.Mapper;

import com.fluxocaixa.dto.HistoricoTransacaoRespostaDTO;
import com.fluxocaixa.entity.HistoricoTransacao;

@Mapper(componentModel = "spring")
public interface HistoricoTransacaoMapper {

    HistoricoTransacaoRespostaDTO toDTO(HistoricoTransacao historicoTransacao);
}
