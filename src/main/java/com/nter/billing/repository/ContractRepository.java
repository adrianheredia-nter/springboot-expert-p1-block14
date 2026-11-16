package com.nter.billing.repository;

import com.nter.billing.entity.ContractJPA;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractRepository extends JpaRepository<ContractJPA, Long> {

    boolean existsByContractNumber(String contractNumber);
}
