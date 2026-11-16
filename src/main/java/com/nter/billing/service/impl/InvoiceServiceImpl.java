package com.nter.billing.service.impl;

import com.nter.billing.constants.AppConstants;
import com.nter.billing.dto.InvoiceRequest;
import com.nter.billing.dto.InvoiceResponse;
import com.nter.billing.entity.ContractJPA;
import com.nter.billing.exception.AlreadyExistsException;
import com.nter.billing.exception.NotFoundException;
import com.nter.billing.mapper.InvoiceMapper;
import com.nter.billing.repository.ContractRepository;
import com.nter.billing.repository.InvoiceRepository;
import com.nter.billing.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ContractRepository contractRepository;
    private final InvoiceMapper invoiceMapper;

    @Override
    public List<InvoiceResponse> findAll() {
        return invoiceRepository.findAll().stream().map(invoiceMapper::toResponse).toList();
    }

    @Override
    public InvoiceResponse findById(Long id) {
        return invoiceRepository.findById(id)
                .map(invoiceMapper::toResponse)
                .orElseThrow(() -> new NotFoundException(AppConstants.INVOICE_NOT_FOUND.formatted(id)));
    }

    @Override
    public List<InvoiceResponse> findByContract(Long contractId) {
        getContract(contractId);
        return invoiceRepository.findByContractId(contractId).stream().map(invoiceMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public InvoiceResponse create(InvoiceRequest request) {
        String invoiceNumber = request.invoiceNumber().trim();
        if (invoiceRepository.existsByInvoiceNumber(invoiceNumber)) {
            throw new AlreadyExistsException(AppConstants.INVOICE_ALREADY_EXISTS.formatted(invoiceNumber));
        }
        ContractJPA contract = getContract(request.contractId());
        return invoiceMapper.toResponse(invoiceRepository.save(invoiceMapper.toEntity(request, contract)));
    }

    private ContractJPA getContract(Long contractId) {
        return contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException(AppConstants.CONTRACT_NOT_FOUND.formatted(contractId)));
    }
}
