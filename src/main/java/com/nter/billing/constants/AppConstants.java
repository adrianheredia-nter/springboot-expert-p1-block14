package com.nter.billing.constants;

public final class AppConstants {

    public static final String AUTH_PATH = "/auth";
    public static final String LOGIN_PATH = AUTH_PATH + "/login";
    public static final String REFRESH_PATH = AUTH_PATH + "/refresh";
    public static final String CHANGE_PASSWORD_PATH = AUTH_PATH + "/user/changepass";
    public static final String CONTRACTS_PATH = "/api/contracts";
    public static final String INVOICES_PATH = "/api/invoices";

    public static final String CONTRACT_NOT_FOUND = "Contract %d not found";
    public static final String CONTRACT_ALREADY_EXISTS = "Contract number %s already exists";
    public static final String INVOICE_NOT_FOUND = "Invoice %d not found";
    public static final String INVOICE_ALREADY_EXISTS = "Invoice number %s already exists";
    public static final String INVALID_CONTRACT_DATES = "endDate must not be before startDate";
    public static final String BAD_CREDENTIALS = "Invalid username or password";
    public static final String SAME_PASSWORD = "The new password must be different from the current one";

    private AppConstants() {
    }
}
