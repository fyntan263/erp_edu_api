package com.innoverse.erp_edu_api.features.income.payments.dto;

import com.innoverse.erp_edu_api.features.income.payments.Payment;
import com.innoverse.erp_edu_api.features.income.payments.jdbc.PaymentEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class PaymentWithAuditDto {
    private Payment payment;
    private Instant createdAt;
    private Instant updatedAt;

    public static PaymentWithAuditDto fromEntity(PaymentEntity entity) {
        return new PaymentWithAuditDto(
                entity.toDomain(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}