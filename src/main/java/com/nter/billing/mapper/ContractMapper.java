package com.nter.billing.mapper;

import com.nter.billing.dto.ContractRequest;
import com.nter.billing.dto.ContractResponse;
import com.nter.billing.entity.ContractJPA;
import org.springframework.stereotype.Component;

@Component
public class ContractMapper {

    public ContractJPA toEntity(ContractRequest request) {
        return ContractJPA.builder()
                .contractNumber(request.contractNumber().trim())
                .customerName(request.customerName().trim())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build();
    }

    public ContractResponse toResponse(ContractJPA contract) {
        return new ContractResponse(
                contract.getId(),
                contract.getContractNumber(),
                contract.getCustomerName(),
                contract.getStartDate(),
                contract.getEndDate());
    }
}
