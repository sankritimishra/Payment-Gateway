package com.example.demo.services;

import com.example.demo.dtos.PaymentHistoryDTO;
import com.example.demo.repositories.IdempotencyRepository;
import com.example.demo.repositories.PaymentHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the idempotency wrapper in PaymentHistoryService. Repositories
 * are mocked so these run without a database and focus purely on the
 * reserve/complete/release state machine described in the class's own
 * Javadoc: NEW -> process, COMPLETED -> return cached response, IN_PROGRESS ->
 * reject, failure -> release reservation.
 */
@ExtendWith(MockitoExtension.class)
class PaymentHistoryServiceTest {

    @Mock
    private PaymentHistoryRepository paymentHistoryRepository;

    @Mock
    private IdempotencyRepository idempotencyRepository;

    private PaymentHistoryService paymentHistoryService;

    private static final String IDEMPOTENCY_KEY = "key-123";
    private static final String SOURCE_ACCOUNT = "1000000001";
    private static final String DEST_ACCOUNT = "2000000002";

    @BeforeEach
    void setUp() {
        // Real ObjectMapper (not mocked) so the serialize/deserialize round-trip
        // used to cache and replay a completed response is actually exercised.
        paymentHistoryService = new PaymentHistoryService(
                paymentHistoryRepository, idempotencyRepository, new ObjectMapper());
    }

    private PaymentHistoryDTO sampleBody() {
        PaymentHistoryDTO body = new PaymentHistoryDTO();
        body.setSourceAccountNumber(SOURCE_ACCOUNT);
        body.setSourceAccountName("Alice");
        body.setDestAccountNumber(DEST_ACCOUNT);
        body.setDestAccountName("Bob");
        body.setAmount(new BigDecimal("500.00"));
        return body;
    }

    @Test
    void makeTransaction_reservesKeyAndProcessesPayment_whenKeyIsNew() throws Exception {
        PaymentHistoryDTO body = sampleBody();
        when(idempotencyRepository.tryReserve(IDEMPOTENCY_KEY)).thenReturn(true);

        PaymentHistoryDTO result = paymentHistoryService.makeTransaction(IDEMPOTENCY_KEY, SOURCE_ACCOUNT, body);

        assertThat(result).isSameAs(body);
        verify(paymentHistoryRepository).makePayment(SOURCE_ACCOUNT, body);
        verify(idempotencyRepository).markCompleted(eq(IDEMPOTENCY_KEY), anyString());
        verify(idempotencyRepository, never()).release(anyString());
    }

    @Test
    void makeTransaction_returnsCachedResponse_whenKeyAlreadyCompleted() throws Exception {
        PaymentHistoryDTO cachedBody = sampleBody();
        cachedBody.setSuccessful(true);
        cachedBody.setMessageSentToSourceAccount("Transaction successful, amount deducted");
        String cachedJson = new ObjectMapper().writeValueAsString(cachedBody);

        when(idempotencyRepository.tryReserve(IDEMPOTENCY_KEY)).thenReturn(false);
        when(idempotencyRepository.find(IDEMPOTENCY_KEY)).thenReturn(Optional.of(
                new IdempotencyRepository.IdempotencyRecordView(IDEMPOTENCY_KEY, "COMPLETED", cachedJson)));

        PaymentHistoryDTO result = paymentHistoryService.makeTransaction(IDEMPOTENCY_KEY, SOURCE_ACCOUNT, sampleBody());

        assertThat(result.getSourceAccountNumber()).isEqualTo(cachedBody.getSourceAccountNumber());
        assertThat(result.getAmount()).isEqualByComparingTo(cachedBody.getAmount());
        assertThat(result.isSuccessful()).isTrue();
        // Reprocessing must never happen - that's the entire point of idempotency.
        verify(paymentHistoryRepository, never()).makePayment(anyString(), any());
    }

    @Test
    void makeTransaction_throwsDuplicateInProgress_whenKeyIsStillProcessing() {
        when(idempotencyRepository.tryReserve(IDEMPOTENCY_KEY)).thenReturn(false);
        when(idempotencyRepository.find(IDEMPOTENCY_KEY)).thenReturn(Optional.of(
                new IdempotencyRepository.IdempotencyRecordView(IDEMPOTENCY_KEY, "IN_PROGRESS", null)));

        assertThatThrownBy(() ->
                paymentHistoryService.makeTransaction(IDEMPOTENCY_KEY, SOURCE_ACCOUNT, sampleBody()))
                .isInstanceOf(PaymentHistoryService.DuplicateRequestInProgressException.class);

        verify(paymentHistoryRepository, never()).makePayment(anyString(), any());
    }

    @Test
    void makeTransaction_releasesReservation_whenPaymentProcessingFails() throws Exception {
        PaymentHistoryDTO body = sampleBody();
        when(idempotencyRepository.tryReserve(IDEMPOTENCY_KEY)).thenReturn(true);
        doThrow(new PaymentHistoryRepository.InsufficientFundsException("insufficient funds"))
                .when(paymentHistoryRepository).makePayment(SOURCE_ACCOUNT, body);

        assertThatThrownBy(() ->
                paymentHistoryService.makeTransaction(IDEMPOTENCY_KEY, SOURCE_ACCOUNT, body))
                .isInstanceOf(PaymentHistoryRepository.InsufficientFundsException.class);

        // So a legitimate retry with the same key isn't blocked forever.
        verify(idempotencyRepository).release(IDEMPOTENCY_KEY);
        verify(idempotencyRepository, never()).markCompleted(anyString(), anyString());
    }

    @Test
    void makeTransaction_throwsIllegalState_whenReservationDisappearsUnexpectedly() {
        when(idempotencyRepository.tryReserve(IDEMPOTENCY_KEY)).thenReturn(false);
        when(idempotencyRepository.find(IDEMPOTENCY_KEY)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                paymentHistoryService.makeTransaction(IDEMPOTENCY_KEY, SOURCE_ACCOUNT, sampleBody()))
                .isInstanceOf(IllegalStateException.class);
    }
}
