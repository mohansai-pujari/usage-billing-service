package com.billing.pricing.calculator;

import com.billing.entity.invoice.CalculatedCharge;
import com.billing.entity.usage.ServiceUsageSummary;

import java.util.List;

public interface BillingCalculator {

    List<CalculatedCharge> calculate(List<ServiceUsageSummary> summaries);

}