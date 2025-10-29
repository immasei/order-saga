package com.example.store.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "app.kafka")
@Getter
@Setter
public class KafkaTopicProperties {
    private Map<String, String> topics;
    private int partitions;
    private short replicas;

    // === Generic helper ===
    public String commandsOf(String base) {
        return topics.get(base) + ".commands";
    }

    public String eventsOf(String base) {
        return topics.get(base) + ".events";
    }

    // === Convenience getters (readable in code) ===
    public String ordersCommands()        { return commandsOf("orders"); }
    public String ordersEvents()          { return eventsOf("orders"); }

    public String inventoryCommands()     { return commandsOf("inventory"); }
    public String inventoryEvents()       { return eventsOf("inventory"); }

    public String paymentsCommands()      { return commandsOf("payments"); }
    public String paymentsEvents()        { return eventsOf("payments"); }

    public String shippingCommands()      { return commandsOf("shipping"); }
    public String shippingEvents()        { return eventsOf("shipping"); }

    public String notificationsCommands() { return commandsOf("notifications"); }
    public String notificationsEvents()   { return eventsOf("notifications"); }
}
