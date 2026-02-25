package com.fluxocaixa.mapper;

import org.mapstruct.Mapper;

import com.fluxocaixa.dto.BudgetResponse;
import com.fluxocaixa.entity.Budget;

@Mapper(componentModel = "spring")
public interface BudgetMapper {

    BudgetResponse toResponse(Budget budget);
}
