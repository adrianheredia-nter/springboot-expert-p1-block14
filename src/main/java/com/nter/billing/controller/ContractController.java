package com.nter.billing.controller;

import com.nter.billing.constants.AppConstants;
import com.nter.billing.dto.ContractRequest;
import com.nter.billing.dto.ContractResponse;
import com.nter.billing.service.ContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(AppConstants.CONTRACTS_PATH)
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @GetMapping
    public List<ContractResponse> findAll() {
        return contractService.findAll();
    }

    @GetMapping("/{id}")
    public ContractResponse findById(@PathVariable Long id) {
        return contractService.findById(id);
    }

    @PostMapping
    public ResponseEntity<ContractResponse> create(@Valid @RequestBody ContractRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contractService.create(request));
    }
}
