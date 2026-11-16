package com.nter.billing.constants;

public final class AppConstants {

    public static final String CONTRACTS_PATH = "/api/contracts";
    public static final String INVOICES_PATH = "/api/invoices";

    public static final String CONTRACT_NOT_FOUND = "Contract %d not found";
    public static final String CONTRACT_ALREADY_EXISTS = "Contract number %s already exists";
    public static final String INVOICE_NOT_FOUND = "Invoice %d not found";
    public static final String INVOICE_ALREADY_EXISTS = "Invoice number %s already exists";
    public static final String INVALID_CONTRACT_DATES = "endDate must not be before startDate";

    private AppConstants() {
    }
}
