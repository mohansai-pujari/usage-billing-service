package com.billing.web.controller;

import com.billing.application.query.InvoiceQuery;
import com.billing.domain.common.BillingPeriod;
import com.billing.domain.enums.CurrencyType;
import com.billing.domain.enums.ServiceType;
import com.billing.service.BillingService;
import com.billing.web.dto.response.InvoiceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@Tag(name = "Invoices", description = "Generate usage-based invoices")
public class InvoiceController {

    private static final Logger log = LoggerFactory.getLogger(InvoiceController.class);

    @Autowired
    private BillingService billingService;

    @GetMapping("/invoices")
    @Operation(summary = "Generate an invoice for a billing period across all users")
    public InvoiceResponse getInvoiceForPeriod(
            @RequestParam long start,
            @RequestParam long end,
            @RequestParam(defaultValue = "USD") CurrencyType currency,
            @RequestParam(required = false) ServiceType serviceType) {

        log.debug("GET /invoices period=[{}, {}) currency={} serviceType={}", start, end, currency, serviceType);
        return invoiceResponse(null, start, end, currency, serviceType);
    }

    @GetMapping("/invoices/{userId}")
    @Operation(summary = "Generate an invoice for a user and billing period")
    public InvoiceResponse getInvoice(
            @PathVariable @NotBlank String userId,
            @RequestParam long start,
            @RequestParam long end,
            @RequestParam(defaultValue = "USD") CurrencyType currency,
            @RequestParam(required = false) ServiceType serviceType) {

        log.debug("GET /invoices/{} period=[{}, {}) currency={} serviceType={}",
                userId, start, end, currency, serviceType);
        return invoiceResponse(userId, start, end, currency, serviceType);
    }

    private InvoiceResponse invoiceResponse(
            String userId,
            long start,
            long end,
            CurrencyType currency,
            ServiceType serviceType) {
        InvoiceQuery query = InvoiceQuery.of(userId, new BillingPeriod(start, end), currency, serviceType);
        return InvoiceResponse.from(billingService.generateInvoice(query), query.currency());
    }
}
