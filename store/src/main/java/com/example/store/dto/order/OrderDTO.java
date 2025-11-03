package com.example.store.dto.order;

import com.example.store.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDTO {

    private String orderNumber;
    private String customerEmail;
    private String deliveryAddress;
    private OrderStatus status;
    private BigDecimal subTotal;
    private BigDecimal shipping;
    private BigDecimal tax;
    private BigDecimal total;
    private UUID deliveryTrackingId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime placedAt;
    private List<OrderItemDTO> orderItems;

}
