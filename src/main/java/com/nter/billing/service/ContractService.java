package com.nter.billing.service;

import com.nter.billing.dto.ContractRequest;
import com.nter.billing.dto.ContractResponse;

import java.util.List;

public interface ContractService {

    List<ContractResponse> findAll();

    ContractResponse findById(Long id);

    ContractResponse create(ContractRequest request);
}
