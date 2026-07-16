package com.billing.pricing.mapper;

import com.billing.config.properties.PricingDefinitionProperties;
import com.billing.config.properties.TierProperties;
import com.billing.entity.common.Money;
import com.billing.entity.pricing.FlatPricingConfig;
import com.billing.entity.pricing.PricingConfig;
import com.billing.entity.pricing.SubscriptionPricingConfig;
import com.billing.entity.pricing.Tier;
import com.billing.entity.pricing.TieredPricingConfig;
import com.billing.enums.BillingType;
import com.billing.enums.ServiceType;
import com.billing.enums.UnitType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PricingConfigMapper {

    public PricingConfig map(ServiceType serviceType, PricingDefinitionProperties properties) {

        BillingType billingType = BillingType.valueOf(properties.getBillingType());

        UnitType unitType = UnitType.valueOf(properties.getUnit());

        return switch (billingType) {
            case FLAT -> buildFlat(serviceType, unitType, properties);
            case TIERED -> buildTiered(serviceType, unitType, properties);
            case SUBSCRIPTION -> buildSubscription(serviceType, unitType, properties);
        };
    }

    private FlatPricingConfig buildFlat(ServiceType serviceType, UnitType unitType,
                                        PricingDefinitionProperties properties) {

        return new FlatPricingConfig(serviceType, unitType, Money.of(properties.getUnitPrice()));
    }

    private TieredPricingConfig buildTiered(ServiceType serviceType, UnitType unitType,
                                            PricingDefinitionProperties properties) {

        List<Tier> tiers = properties
                .getTiers()
                .stream()
                .map(this::mapTier)
                .collect(Collectors.toList());

        return new TieredPricingConfig(serviceType, unitType, tiers);
    }

    private SubscriptionPricingConfig buildSubscription(ServiceType serviceType, UnitType unitType,
                                                        PricingDefinitionProperties properties) {

        return new SubscriptionPricingConfig(
                serviceType,
                unitType,
                Money.of(properties.getMonthlyFee()),
                properties.getIncludedUnits(),
                Money.of(properties.getOverageUnitPrice())
        );
    }

    private Tier mapTier(TierProperties properties) {

        return new Tier(properties.getUpTo(), Money.of(properties.getUnitPrice()));
    }
}