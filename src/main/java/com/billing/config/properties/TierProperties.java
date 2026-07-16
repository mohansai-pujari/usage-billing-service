package com.billing.config.properties;

import java.math.BigDecimal;

public class TierProperties {

    private Long upTo;

    private BigDecimal unitPrice;

    public Long getUpTo() {
        return upTo;
    }

    public void setUpTo(Long upTo) {
        this.upTo = upTo;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}