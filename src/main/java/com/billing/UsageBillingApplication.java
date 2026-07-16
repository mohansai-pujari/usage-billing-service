package com.billing;

import com.billing.config.BillingProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(BillingProperties.class)
public class UsageBillingApplication {

    public static void main(String[] args) {
        SpringApplication.run(UsageBillingApplication.class, args);
    }
}
