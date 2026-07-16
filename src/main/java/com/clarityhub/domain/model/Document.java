package com.clarityhub.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * A document that a user has uploaded.
 *
 * <p>A document belongs to one tenant (the customer organization) and has one owner (the user who
 * uploaded it). It is what other users can be given permission to read.
 *
 * <p>Fields:
 *
 * <ul>
 *   <li>{@code id} — the document's own id.
 *   <li>{@code tenantId} — which customer organization owns it. Used in searches to make sure one
 *       customer's data can never show up in another customer's results.
 *   <li>{@code ownerId} — the user who uploaded the document. Automatically gets permission to read
 *       it.
 *   <li>{@code title} — a short name shown to users. Cannot be empty or just whitespace.
 *   <li>{@code createdAt} — when the document was saved. We use {@link Instant} because it is
 *       always in UTC and never depends on the server's local time zone.
 * </ul>
 *
 * <p>Cannot change after creation. All the checks below run once, when the document is created.
 */
public record Document(
        DocumentId id, TenantId tenantId, UserId ownerId, String title, Instant createdAt) {

    /**
     * Rejects any missing field, and also rejects a title that is empty or only whitespace. If a
     * document is created successfully, everywhere else in the code can trust that all fields are
     * valid.
     */
    public Document {
        Objects.requireNonNull(id, "Document id must not be null");
        Objects.requireNonNull(tenantId, "Document tenantId must not be null");
        Objects.requireNonNull(ownerId, "Document ownerId must not be null");
        Objects.requireNonNull(title, "Document title must not be null");
        Objects.requireNonNull(createdAt, "Document createdAt must not be null");
        if (title.isBlank()) {
            throw new IllegalArgumentException("Document title must not be blank");
        }
    }
}
