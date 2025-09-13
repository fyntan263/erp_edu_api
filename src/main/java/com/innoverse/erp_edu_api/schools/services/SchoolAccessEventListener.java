package com.innoverse.erp_edu_api.schools.services;

import com.innoverse.erp_edu_api.provisioning.SchoolAccessCheckEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchoolAccessEventListener {
    private final SchoolRepository repository; // Now this is safe
    @Async
    @EventListener
    public void handleSchoolAccessCheckEvent(SchoolAccessCheckEvent event) {
        UUID schoolId = event.getSchoolId();
        log.info("Received access check request for school: {}", schoolId);
        try {
            boolean isSchoolActive = repository.findById(schoolId)
                    .filter(school -> "active".equalsIgnoreCase(school.getStatus()))
                    .isPresent();
            event.complete(isSchoolActive);

        } catch (Exception e) {
            log.error("Error processing access check for school: {}", schoolId, e);
            event.complete(false);
        }
    }
}