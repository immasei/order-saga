package com.deliveryco.config.scheduler;

import com.deliveryco.domain.service.JobExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryJobWorker {

    private final JobExecutionService jobExecutionService;

    @Scheduled(fixedDelayString = "PT2S")
    public void poll() {
        try {
            jobExecutionService.processDueJobs();
        } catch (Exception ex) {
            log.error("Failed to process delivery jobs", ex);
        }
    }
}

