package com.nter.billing.config;

import com.nter.billing.entity.ContractJPA;
import com.nter.billing.entity.InvoiceJPA;
import com.nter.billing.repository.ContractRepository;
import com.nter.billing.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class InitializeData implements ApplicationRunner {

    private final ContractRepository contractRepository;
    private final InvoiceRepository invoiceRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        loadContractsAndInvoices();
    }

    private void loadContractsAndInvoices() {
        ContractJPA maintenance = contractRepository.save(ContractJPA.builder()
                .contractNumber("CT-2024-001")
                .customerName("Acme Logistics")
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .build());
        ContractJPA hosting = contractRepository.save(ContractJPA.builder()
                .contractNumber("CT-2024-002")
                .customerName("Blue Harbor Foods")
                .startDate(LocalDate.of(2024, 6, 15))
                .build());

        invoiceRepository.save(invoice("INV-2024-0001", LocalDate.of(2024, 1, 31), "1250.00", maintenance));
        invoiceRepository.save(invoice("INV-2024-0002", LocalDate.of(2024, 2, 29), "1250.00", maintenance));
        invoiceRepository.save(invoice("INV-2024-0003", LocalDate.of(2024, 6, 30), "480.50", hosting));
    }

    private InvoiceJPA invoice(String number, LocalDate issueDate, String amount, ContractJPA contract) {
        return InvoiceJPA.builder()
                .invoiceNumber(number)
                .issueDate(issueDate)
                .amount(new BigDecimal(amount))
                .contract(contract)
                .build();
    }
}
