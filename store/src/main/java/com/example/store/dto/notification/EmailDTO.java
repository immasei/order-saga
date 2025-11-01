package com.example.store.dto.notification;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class EmailDTO {
    private String toAddress;
    private String externalOrderId;
    private String subject;
    private String body;
}