package com.innoverse.erp_edu_api.provisioning;

import lombok.Getter;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
@Getter
public class SchoolAccessCheckEvent{
    private final UUID schoolId;
    private final CompletableFuture<Boolean> accessFuture;

    public SchoolAccessCheckEvent(UUID schoolId) {
        this.schoolId = schoolId;
        this.accessFuture = new CompletableFuture<>();
    }

    public void complete(boolean hasAccess) {
        this.accessFuture.complete(hasAccess);
    }
}
