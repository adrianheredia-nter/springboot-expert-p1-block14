package com.nter.billing.service.impl;

import com.nter.billing.constants.AppConstants;
import com.nter.billing.dto.ContractRequest;
import com.nter.billing.dto.ContractResponse;
import com.nter.billing.exception.AlreadyExistsException;
import com.nter.billing.exception.InvalidDataException;
import com.nter.billing.exception.NotFoundException;
import com.nter.billing.mapper.ContractMapper;
import com.nter.billing.repository.ContractRepository;
import com.nter.billing.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final ContractMapper contractMapper;

    @Override
    public List<ContractResponse> findAll() {
        return contractRepository.findAll().stream().map(contractMapper::toResponse).toList();
    }

    @Override
    public ContractResponse findById(Long id) {
        return contractRepository.findById(id)
                .map(contractMapper::toResponse)
                .orElseThrow(() -> new NotFoundException(AppConstants.CONTRACT_NOT_FOUND.formatted(id)));
    }

    @Override
    @Transactional
    public ContractResponse create(ContractRequest request) {
        if (request.endDate() != null && request.endDate().isBefore(request.startDate())) {
            throw new InvalidDataException(AppConstants.INVALID_CONTRACT_DATES);
        }
        String contractNumber = request.contractNumber().trim();
        if (contractRepository.existsByContractNumber(contractNumber)) {
            throw new AlreadyExistsException(AppConstants.CONTRACT_ALREADY_EXISTS.formatted(contractNumber));
        }
        return contractMapper.toResponse(contractRepository.save(contractMapper.toEntity(request)));
    }
}
