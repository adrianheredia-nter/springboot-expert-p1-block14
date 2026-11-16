package com.nter.billing.dto;

import java.time.LocalDate;

public record ContractResponse(Long id, String contractNumber, String customerName, LocalDate startDate,
                               LocalDate endDate) {
}
