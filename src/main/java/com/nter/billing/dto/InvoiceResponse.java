package com.nter.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceResponse(Long id, String invoiceNumber, LocalDate issueDate, BigDecimal amount, Long contractId) {
}
