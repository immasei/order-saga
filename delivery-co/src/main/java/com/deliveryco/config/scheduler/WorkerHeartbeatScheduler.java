package com.deliveryco.config.scheduler;

import com.deliveryco.domain.service.WorkerHeartbeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkerHeartbeatScheduler {

    private final WorkerHeartbeatService heartbeatService;

    @Scheduled(fixedDelayString = "PT5S")
    public void jobWorkerHeartbeat() {
        heartbeatService.beat("JOB_WORKER");
    }

    @Scheduled(fixedDelayString = "PT5S", initialDelayString = "PT2S")
    public void outboxWorkerHeartbeat() {
        heartbeatService.beat("OUTBOX_WORKER");
    }
}

