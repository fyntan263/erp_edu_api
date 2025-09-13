//package com.innoverse.erp_edu_api.features.income.invoices;
//
//import com.innoverse.erp_edu_api.features.income.invoices.Invoice;
//import com.innoverse.erp_edu_api.features.income.invoices.dto.*;
//import com.innoverse.erp_edu_api.features.income.invoices.services.InvoiceService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/api/schools/invoices")
//@RequiredArgsConstructor
//public class InvoiceController {
//
//    private final InvoiceService invoiceService;
//
//    @PostMapping
//    public ResponseEntity<InvoiceResponse> createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
//        var invoice = invoiceService.createInvoice(request);
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(InvoiceResponse.fromDomain(invoice));
//    }
//
//    @GetMapping("/{invoiceId}")
//    public ResponseEntity<InvoiceResponse> getInvoice(@PathVariable UUID invoiceId) {
//        return invoiceService.getInvoiceById(invoiceId)
//                .map(invoice -> ResponseEntity.ok(InvoiceResponse.fromDomain(invoice)))
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    @GetMapping("/number/{invoiceNo}")
//    public ResponseEntity<InvoiceResponse> getInvoiceByNumber(@PathVariable String invoiceNo) {
//        return invoiceService.getInvoiceByNumber(invoiceNo)
//                .map(invoice -> ResponseEntity.ok(InvoiceResponse.fromDomain(invoice)))
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    @GetMapping("/student/{studentId}")
//    public ResponseEntity<List<InvoiceResponse>> getStudentInvoices(@PathVariable UUID studentId) {
//        var invoices = invoiceService.getInvoicesByStudentId(studentId);
//        var response = invoices.stream()
//                .map(InvoiceResponse::fromDomain)
//                .collect(Collectors.toList());
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping("/status/{status}")
//    public ResponseEntity<List<InvoiceResponse>> getInvoicesByStatus(@PathVariable Invoice.Status status) {
//        var invoices = invoiceService.getInvoicesByStatus(status);
//        var response = invoices.stream()
//                .map(InvoiceResponse::fromDomain)
//                .collect(Collectors.toList());
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping("/overdue")
//    public ResponseEntity<List<InvoiceResponse>> getOverdueInvoices() {
//        var invoices = invoiceService.getOverdueInvoices();
//        var response = invoices.stream()
//                .map(InvoiceResponse::fromDomain)
//                .collect(Collectors.toList());
//        return ResponseEntity.ok(response);
//    }
//
//    @PostMapping("/{invoiceId}/line-items")
//    public ResponseEntity<InvoiceResponse> addLineItem(
//            @PathVariable UUID invoiceId,
//            @Valid @RequestBody InvoiceItemRequest request) {
//        var invoice = invoiceService.addLineItem(invoiceId, request);
//        return ResponseEntity.ok(InvoiceResponse.fromDomain(invoice));
//    }
//
//    @DeleteMapping("/{invoiceId}/line-items/{lineItemId}")
//    public ResponseEntity<InvoiceResponse> removeLineItem(
//            @PathVariable UUID invoiceId,
//            @PathVariable UUID lineItemId) {
//        var invoice = invoiceService.removeLineItem(invoiceId, lineItemId);
//        return ResponseEntity.ok(InvoiceResponse.fromDomain(invoice));
//    }
//
//    @PostMapping("/{invoiceId}/payments")
//    public ResponseEntity<InvoiceResponse> applyPayment(
//            @PathVariable UUID invoiceId,
//            @Valid @RequestBody ApplyPaymentRequest request) {
//        var invoice = invoiceService.applyPayment(invoiceId, request);
//        return ResponseEntity.ok(InvoiceResponse.fromDomain(invoice));
//    }
//
//    @PatchMapping("/{invoiceId}/status/{status}")
//    public ResponseEntity<InvoiceResponse> updateStatus(
//            @PathVariable UUID invoiceId,
//            @PathVariable Invoice.Status status) {
//        var invoice = invoiceService.updateStatus(invoiceId, status);
//        return ResponseEntity.ok(InvoiceResponse.fromDomain(invoice));
//    }
//
//    @DeleteMapping("/{invoiceId}")
//    public ResponseEntity<Void> deleteInvoice(@PathVariable UUID invoiceId) {
//        invoiceService.deleteInvoice(invoiceId);
//        return ResponseEntity.noContent().build();
//    }
//
//    @GetMapping("/student/{studentId}/summary")
//    public ResponseEntity<InvoiceSummaryResponse> getStudentSummary(@PathVariable UUID studentId) {
//        var summary = invoiceService.getStudentInvoiceSummary(studentId);
//        return ResponseEntity.ok(summary);
//    }
//
//    @GetMapping("/student/{studentId}/balance")
//    public ResponseEntity<BigDecimal> getStudentBalance(@PathVariable UUID studentId) {
//        var balance = invoiceService.getStudentOutstandingBalance(studentId);
//        return ResponseEntity.ok(balance);
//    }
//}
