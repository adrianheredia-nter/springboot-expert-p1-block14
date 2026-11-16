package com.nter.billing.config;

import com.nter.billing.entity.ContractJPA;
import com.nter.billing.entity.InvoiceJPA;
import com.nter.billing.entity.Role;
import com.nter.billing.entity.UserJPA;
import com.nter.billing.repository.ContractRepository;
import com.nter.billing.repository.InvoiceRepository;
import com.nter.billing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class InitializeData implements ApplicationRunner {

    public static final String DEFAULT_PASSWORD = "Nter.2024";

    private final ContractRepository contractRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        loadUsers();
        loadContractsAndInvoices();
    }

    private void loadUsers() {
        String password = passwordEncoder.encode(DEFAULT_PASSWORD);
        userRepository.save(user("admin", password, false, EnumSet.of(Role.ADMIN)));
        userRepository.save(user("contract.reader", password, false, EnumSet.of(Role.READ_CONTRACT)));
        userRepository.save(user("contract.writer", password, false, EnumSet.of(Role.WRITE_CONTRACT)));
        userRepository.save(user("invoice.reader", password, false, EnumSet.of(Role.READ_INVOICE)));
        userRepository.save(user("invoice.writer", password, false, EnumSet.of(Role.WRITE_INVOICE)));
        userRepository.save(user("expired.user", password, true, EnumSet.of(Role.READ_CONTRACT, Role.READ_INVOICE)));
    }

    private UserJPA user(String username, String password, boolean passwordExpired, Set<Role> roles) {
        return UserJPA.builder()
                .username(username)
                .password(password)
                .passwordExpired(passwordExpired)
                .roles(roles)
                .build();
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
