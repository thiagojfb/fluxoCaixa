package com.fluxocaixa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.fluxocaixa.dto.TransactionResponse;
import com.fluxocaixa.entity.Transaction;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(source = "tipo", target = "type")
    @Mapping(source = "descricao", target = "description")
    @Mapping(source = "valor", target = "amount")
    @Mapping(source = "dataHora", target = "dateTime")
    @Mapping(source = "criadoEm", target = "createdAt")
    TransactionResponse toResponse(Transaction transaction);
}
