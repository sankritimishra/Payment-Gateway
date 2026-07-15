package com.example.demo.services;

import com.example.demo.dtos.PaymentHistoryDTO;
import com.example.demo.repositories.PaymentHistoryRepository;
import org.springframework.stereotype.Service;
import com.example.demo.repositories.IdempotencyRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PaymentHistoryService {

    private final PaymentHistoryRepository paymentHistoryRepository;
    private final IdempotencyRepository idempotencyRepository;
    private final ObjectMapper objectMapper;

    public PaymentHistoryService(PaymentHistoryRepository paymentHistoryRepository,
                                 IdempotencyRepository idempotencyRepository,
                                 ObjectMapper objectMapper) {
        this.paymentHistoryRepository = paymentHistoryRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.objectMapper = objectMapper;
    }

    public static class DuplicateRequestInProgressException extends RuntimeException {
        public DuplicateRequestInProgressException(String message) {
            super(message);
        }
    }

    /**
     * Wraps makePayment with idempotency handling.
     *
     * Flow:
     * 1. Try to reserve the idempotency key (atomic insert). If it already exists:
     *    - COMPLETED  -> return the cached response, do NOT reprocess
     *    - IN_PROGRESS -> another request with the same key is currently being processed; reject
     * 2. If reservation succeeds, process the payment normally.
     * 3. On success, cache the response body against the key.
     * 4. On failure, release the reservation so the client can safely retry with the same key.
     */
    public PaymentHistoryDTO makeTransaction(String idempotencyKey,String sourceAccountNumber,
                                             PaymentHistoryDTO body) throws Exception {

        boolean reserved = idempotencyRepository.tryReserve(idempotencyKey);

        if (!reserved) {
            var existing = idempotencyRepository.find(idempotencyKey)
                    .orElseThrow(() -> new IllegalStateException("Idempotency key reservation failed unexpectedly"));

            if (existing.isCompleted()) {
                return deserialize(existing.getResponseBody());
            }
            // IN_PROGRESS: a concurrent request with this same key is still being processed
            throw new DuplicateRequestInProgressException(
                    "A request with this idempotency key is already being processed");
        }

        try {
            paymentHistoryRepository.makePayment(sourceAccountNumber, body);
            idempotencyRepository.markCompleted(idempotencyKey, serialize(body));
            return body;
        } catch (Exception e) {
            // Release the reservation so a legitimate retry with the same key isn't blocked forever
            idempotencyRepository.release(idempotencyKey);
            throw e;
        }
    }

    private String serialize(PaymentHistoryDTO body) throws JsonProcessingException {
        return objectMapper.writeValueAsString(body);
    }

    private PaymentHistoryDTO deserialize(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, PaymentHistoryDTO.class);
    }
}
