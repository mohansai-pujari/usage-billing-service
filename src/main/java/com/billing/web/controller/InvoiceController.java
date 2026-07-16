package com.billing.web.controller;

import com.billing.domain.common.BillingPeriod;
import com.billing.domain.enums.CurrencyType;
import com.billing.service.BillingService;
import com.billing.web.dto.response.InvoiceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** HTTP endpoint for invoice generation. */
@RestController
@Validated
@Tag(name = "Invoices", description = "Generate usage-based invoices")
public class InvoiceController {

    private static final Logger log = LoggerFactory.getLogger(InvoiceController.class);

    private final BillingService billingService;

    public InvoiceController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping("/invoices/{userId}")
    @Operation(summary = "Generate an invoice for a user and billing period")
    public InvoiceResponse getInvoice(
            @PathVariable @NotBlank String userId,
            @RequestParam long start,
            @RequestParam long end,
            @RequestParam(defaultValue = "USD") CurrencyType currency) {

        log.debug("GET /invoices/{} period=[{}, {}) currency={}", userId, start, end, currency);
        BillingPeriod period = new BillingPeriod(start, end);
        return InvoiceResponse.from(
                billingService.generateInvoice(userId, period, currency),
                currency);
    }
}
