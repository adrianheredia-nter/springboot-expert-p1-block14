package com.nter.billing.repository;

import com.nter.billing.entity.InvoiceJPA;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<InvoiceJPA, Long> {

    @Override
    @EntityGraph(attributePaths = "contract")
    List<InvoiceJPA> findAll();

    @Override
    @EntityGraph(attributePaths = "contract")
    Optional<InvoiceJPA> findById(Long id);

    @EntityGraph(attributePaths = "contract")
    List<InvoiceJPA> findByContractId(Long contractId);

    boolean existsByInvoiceNumber(String invoiceNumber);
}
