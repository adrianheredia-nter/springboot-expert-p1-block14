package com.nter.billing.mapper;

import com.nter.billing.dto.InvoiceRequest;
import com.nter.billing.dto.InvoiceResponse;
import com.nter.billing.entity.ContractJPA;
import com.nter.billing.entity.InvoiceJPA;
import org.springframework.stereotype.Component;

@Component
public class InvoiceMapper {

    public InvoiceJPA toEntity(InvoiceRequest request, ContractJPA contract) {
        return InvoiceJPA.builder()
                .invoiceNumber(request.invoiceNumber().trim())
                .issueDate(request.issueDate())
                .amount(request.amount())
                .contract(contract)
                .build();
    }

    public InvoiceResponse toResponse(InvoiceJPA invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getIssueDate(),
                invoice.getAmount(),
                invoice.getContract().getId());
    }
}
