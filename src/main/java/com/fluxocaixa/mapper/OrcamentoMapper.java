package com.fluxocaixa.mapper;

import org.mapstruct.Mapper;

import com.fluxocaixa.dto.OrcamentoRespostaDTO;
import com.fluxocaixa.entity.Orcamento;

@Mapper(componentModel = "spring")
public interface OrcamentoMapper {

    OrcamentoRespostaDTO toDTO(Orcamento orcamento);
}
