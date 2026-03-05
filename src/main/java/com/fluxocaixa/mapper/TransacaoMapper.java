package com.fluxocaixa.mapper;

import org.mapstruct.Mapper;

import com.fluxocaixa.dto.TransacaoRespostaDTO;
import com.fluxocaixa.entity.Transacao;

@Mapper(componentModel = "spring")
public interface TransacaoMapper {

    TransacaoRespostaDTO toDTO(Transacao transacao);
}
