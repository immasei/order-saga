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
        private boolean aftOrderPlacedBe4Reserved;
        private boolean aftReservedBe4Paid;
        private boolean aftPaidBe4Shipped;
        private boolean aftShipped;
        private boolean aftPaymentFailed;
        private boolean aftShipmentFailed;
        private boolean aftCancelled;
    }

    @Getter @Setter
    public static class Refund {
        private boolean failed;
    }
}

