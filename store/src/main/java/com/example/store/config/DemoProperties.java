package com.example.store.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "demo")
@Getter
@Setter
public class DemoProperties {

    private Cancelled cancelled = new Cancelled();
    private Refund refund = new Refund();

    @Getter @Setter
    public static class Cancelled {
        private boolean aftOrderPlacedBe4Reserved = false;
        private boolean aftReservedBe4Paid = false;
        private boolean aftPaidBe4Shipped = false;
        private boolean aftShipped = false;
        private boolean aftPaymentFailed = false;
        private boolean aftShipmentFailed = false;
        private boolean aftCancelled = false;
        private boolean aftDeliveryLost = false;
    }

    @Getter @Setter
    public static class Refund {
        private boolean failed = false;
    }
}

