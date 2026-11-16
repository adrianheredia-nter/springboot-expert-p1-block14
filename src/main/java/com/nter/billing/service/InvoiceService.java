package com.nter.billing.service;

import com.nter.billing.dto.InvoiceRequest;
import com.nter.billing.dto.InvoiceResponse;

import java.util.List;

public interface InvoiceService {

    List<InvoiceResponse> findAll();

    InvoiceResponse findById(Long id);

    List<InvoiceResponse> findByContract(Long contractId);

    InvoiceResponse create(InvoiceRequest request);
}
