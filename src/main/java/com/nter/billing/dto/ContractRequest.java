package com.nter.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ContractRequest(
        @NotBlank @Size(max = 30) String contractNumber,
        @NotBlank @Size(max = 100) String customerName,
        @NotNull LocalDate startDate,
        LocalDate endDate) {
}
