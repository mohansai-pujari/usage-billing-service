package com.billing.controller;

import com.billing.dto.response.InvoiceResponse;
import com.billing.entity.common.BillingPeriod;
import com.billing.entity.common.UserId;
import com.billing.entity.invoice.Invoice;
import com.billing.exception.InvalidRequestException;
import com.billing.service.BillingService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InvoiceController {

    private final BillingService billingService;

    public InvoiceController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping("/invoices/{userId}")
    public InvoiceResponse getInvoice(@PathVariable @NotBlank String userId,
                                      @RequestParam long start,
                                      @RequestParam long end) {

        if (userId == null || userId.isBlank()) {
            throw new InvalidRequestException("User id is required.");
        }

        if (start < 0 || end < 0) {
            throw new InvalidRequestException("Start and end timestamps must be non-negative.");
        }

        Invoice invoice = billingService.generateInvoice(UserId.of(userId), new BillingPeriod(start, end));

        return InvoiceResponse.from(invoice);
    }
}
