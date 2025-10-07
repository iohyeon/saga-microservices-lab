package com.example.saga.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class SagaEvent {
    private String requestId;
    private LocalDateTime timestamp;
    private String eventType;
}