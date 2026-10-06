package com.foodordering.paymentservice.controller;

import com.foodordering.paymentservice.dto.PaymentRequest;
import com.foodordering.paymentservice.dto.PaymentResponse;
import com.foodordering.paymentservice.model.Payment;
import com.foodordering.paymentservice.repository.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
        String status = (request.getAmount() != null && request.getAmount() > 0) ? "SUCCESS" : "FAILED";

        Payment payment = new Payment(
                request.getOrderId(),
                request.getAmount() != null ? request.getAmount() : 0.0,
                status,
                LocalDateTime.now()
        );

        Payment saved = paymentRepository.save(payment);

        PaymentResponse response = new PaymentResponse(
                saved.getPaymentId(),
                saved.getOrderId(),
                saved.getAmount(),
                saved.getPaymentStatus(),
                saved.getPaymentDate()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        return paymentRepository.findById(id)
                .map(p -> ResponseEntity.ok(new PaymentResponse(
                        p.getPaymentId(), p.getOrderId(), p.getAmount(), p.getPaymentStatus(), p.getPaymentDate())))
                .orElse(ResponseEntity.notFound().build());
    }
}
