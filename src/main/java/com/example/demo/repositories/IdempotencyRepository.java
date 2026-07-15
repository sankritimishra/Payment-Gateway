package com.example.demo.repositories;


import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class IdempotencyRepository {

    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String STATUS_COMPLETED = "COMPLETED";

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public IdempotencyRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    /**
     * Attempts to reserve an idempotency key by inserting an IN_PROGRESS row.
     * Returns false if the key already exists (duplicate request).
     * Relies on idempotency_key being the PRIMARY KEY to enforce uniqueness at the DB level,
     * so this is safe even under concurrent requests with the same key.
     */
    public boolean tryReserve(String idempotencyKey) {
        String sql = "INSERT IGNORE INTO idempotency_record (idempotency_key, status) " +
                "VALUES (:idempotencyKey, :status)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idempotencyKey", idempotencyKey)
                .addValue("status", STATUS_IN_PROGRESS);

        int rowsInserted = namedParameterJdbcTemplate.update(sql, params);
        return rowsInserted > 0; // false means key already existed
    }

    public Optional<IdempotencyRecordView> find(String idempotencyKey) {
        String sql = "SELECT idempotency_key, status, response_body FROM idempotency_record " +
                "WHERE idempotency_key = :idempotencyKey";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idempotencyKey", idempotencyKey);

        List<IdempotencyRecordView> results = namedParameterJdbcTemplate.query(sql, params,
                (rs, rowNum) -> new IdempotencyRecordView(
                        rs.getString("idempotency_key"),
                        rs.getString("status"),
                        rs.getString("response_body")
                ));

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public void markCompleted(String idempotencyKey, String responseBody) {
        String sql = "UPDATE idempotency_record SET status = :status, response_body = :responseBody " +
                "WHERE idempotency_key = :idempotencyKey";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("status", STATUS_COMPLETED)
                .addValue("responseBody", responseBody)
                .addValue("idempotencyKey", idempotencyKey);

        namedParameterJdbcTemplate.update(sql, params);
    }

    /**
     * Removes the reservation if processing failed, so the client can safely retry
     * with the same idempotency key instead of being stuck with a dead IN_PROGRESS row.
     */
    public void release(String idempotencyKey) {
        String sql = "DELETE FROM idempotency_record WHERE idempotency_key = :idempotencyKey " +
                "AND status = :status";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idempotencyKey", idempotencyKey)
                .addValue("status", STATUS_IN_PROGRESS);

        namedParameterJdbcTemplate.update(sql, params);
    }

    public static class IdempotencyRecordView {
        private final String idempotencyKey;
        private final String status;
        private final String responseBody;

        public IdempotencyRecordView(String idempotencyKey, String status, String responseBody) {
            this.idempotencyKey = idempotencyKey;
            this.status = status;
            this.responseBody = responseBody;
        }

        public String getIdempotencyKey() {
            return idempotencyKey;
        }

        public String getStatus() {
            return status;
        }

        public String getResponseBody() {
            return responseBody;
        }

        public boolean isCompleted() {
            return "COMPLETED".equals(status);
        }

        public boolean isInProgress() {
            return "IN_PROGRESS".equals(status);
        }
    }
}