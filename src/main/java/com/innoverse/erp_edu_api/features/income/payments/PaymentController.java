package com.innoverse.erp_edu_api.features.income.payments;


import com.innoverse.erp_edu_api.features.income.payments.dto.CreatePaymentRequest;
import com.innoverse.erp_edu_api.features.income.payments.dto.PaymentPatchRequest;
import com.innoverse.erp_edu_api.features.income.payments.dto.PaymentRequest;
import com.innoverse.erp_edu_api.features.income.payments.dto.PaymentResponse;
import com.innoverse.erp_edu_api.features.income.payments.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/schools/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        Payment payment = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PaymentResponse.fromDomain(payment));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID paymentId) {
        return paymentService.getPaymentById(paymentId)
                .map(payment -> ResponseEntity.ok(PaymentResponse.fromDomain(payment)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{paymentNo}")
    public ResponseEntity<PaymentResponse> getPaymentByNumber(@PathVariable String paymentNo) {
        return paymentService.getPaymentByNumber(paymentNo)
                .map(payment -> ResponseEntity.ok(PaymentResponse.fromDomain(payment)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<PaymentResponse>> getAllPayments(Pageable pageable) {
        Page<Payment> payments = paymentService.getAllPayments(pageable);
        Page<PaymentResponse> response = payments.map(PaymentResponse::fromDomain);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<PaymentResponse>> getStudentPayments(@PathVariable UUID studentId) {
        List<Payment> payments = paymentService.getPaymentsByEntityId(studentId);
        List<PaymentResponse> response = payments.stream()
                .map(PaymentResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<List<PaymentResponse>> getInvoicePayments(@PathVariable UUID invoiceId) {
        List<Payment> payments = paymentService.getPaymentsByInvoiceId(invoiceId);
        List<PaymentResponse> response = payments.stream()
                .map(PaymentResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(@PathVariable Payment.PaymentStatus status) {
        List<Payment> payments = paymentService.getPaymentsByStatus(status);
        List<PaymentResponse> response = payments.stream()
                .map(PaymentResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> updatePayment(
            @PathVariable UUID paymentId,
            @Valid @RequestBody PaymentPatchRequest request) {

        Payment payment;
        if (request.status() != null) {
            payment = paymentService.updatePaymentStatus(paymentId, request.status(), request.paymentNotes());
        } else if (request.paymentNotes() != null) {
            payment = paymentService.updatePaymentNotes(paymentId, request.paymentNotes());
        } else {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(PaymentResponse.fromDomain(payment));
    }

    @PostMapping("/{paymentId}/complete")
    public ResponseEntity<PaymentResponse> markAsCompleted(@PathVariable UUID paymentId) {
        Payment payment = paymentService.markAsCompleted(paymentId);
        return ResponseEntity.ok(PaymentResponse.fromDomain(payment));
    }

    @PostMapping("/{paymentId}/fail")
    public ResponseEntity<PaymentResponse> markAsFailed(
            @PathVariable UUID paymentId,
            @RequestParam String reason) {
        Payment payment = paymentService.markAsFailed(paymentId, reason);
        return ResponseEntity.ok(PaymentResponse.fromDomain(payment));
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentResponse> markAsRefunded(@PathVariable UUID paymentId) {
        Payment payment = paymentService.markAsRefunded(paymentId);
        return ResponseEntity.ok(PaymentResponse.fromDomain(payment));
    }

    @PostMapping("/{paymentId}/refund/partial")
    public ResponseEntity<PaymentResponse> markAsPartiallyRefunded(@PathVariable UUID paymentId) {
        Payment payment = paymentService.markAsPartiallyRefunded(paymentId);
        return ResponseEntity.ok(PaymentResponse.fromDomain(payment));
    }

    @DeleteMapping("/{paymentId}")
    public ResponseEntity<Void> deletePayment(@PathVariable UUID paymentId) {
        paymentService.softDeletePayment(paymentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{paymentId}/restore")
    public ResponseEntity<Void> restorePayment(@PathVariable UUID paymentId) {
        paymentService.restorePayment(paymentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/exists/{paymentNo}")
    public ResponseEntity<Boolean> paymentExists(@PathVariable String paymentNo) {
        boolean exists = paymentService.paymentExists(paymentNo);
        return ResponseEntity.ok(exists);
    }


}
