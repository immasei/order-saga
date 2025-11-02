package com.example.store.dto.notification;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EmailResponseDTO {
    private String toAddress;
    private String externalOrderId;
    private String subject;
    private String body;

    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
}
