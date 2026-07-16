package com.billing.assembler;

import com.billing.entity.common.BillingPeriod;
import com.billing.entity.common.Money;
import com.billing.entity.common.UserId;
import com.billing.entity.invoice.CalculatedCharge;
import com.billing.entity.invoice.Invoice;
import com.billing.entity.invoice.InvoiceLineItem;
import com.billing.entity.invoice.ServiceSubtotal;
import com.billing.entity.usage.ResourceUsageSummary;
import com.billing.entity.usage.ServiceUsageSummary;
import com.billing.enums.ServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class InvoiceAssembler {

    private static final Logger log = LoggerFactory.getLogger(InvoiceAssembler.class);

    public Invoice assemble(UserId userId, BillingPeriod billingPeriod, List<CalculatedCharge> charges) {
        List<InvoiceLineItem> lineItems = new ArrayList<>();
        Map<ServiceType, List<InvoiceLineItem>> serviceLines = new EnumMap<>(ServiceType.class);
        Map<ServiceType, Money> serviceTotals = new EnumMap<>(ServiceType.class);

        for (CalculatedCharge charge : charges) {
            ServiceUsageSummary summary = charge.getUsageSummary();
            ServiceType serviceType = summary.getServiceType();

            for (ResourceUsageSummary resourceUsage : summary.getResourceUsages()) {
                InvoiceLineItem lineItem = new InvoiceLineItem(
                        resourceUsage.getDimension().getResourceId().toString(),
                        serviceType.name() + " usage",
                        resourceUsage.getTotalQuantity().value(),
                        summary.getUnitType(),
                        charge.getAmount()
                );
                lineItems.add(lineItem);
                serviceLines.computeIfAbsent(serviceType, ignored -> new ArrayList<>()).add(lineItem);
            }

            serviceTotals.merge(serviceType, charge.getAmount(), Money::add);
        }

        log.debug("Assembling invoice for user {} with {} charge entries", userId, charges.size());
        List<ServiceSubtotal> subtotals = new ArrayList<>();
        Money total = Money.zero();
        for (Map.Entry<ServiceType, Money> entry : serviceTotals.entrySet()) {
            total = total.add(entry.getValue());
            subtotals.add(new ServiceSubtotal(entry.getKey(), entry.getValue(), List.copyOf(serviceLines.get(entry.getKey()))));
        }

        return new Invoice(userId, billingPeriod, List.copyOf(lineItems), List.copyOf(subtotals), total);
    }
}
