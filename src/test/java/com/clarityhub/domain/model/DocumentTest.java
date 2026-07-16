package com.clarityhub.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Checks that {@link Document} accepts valid input and rejects every kind of invalid input. There
 * is one test per rule, so a red test tells you exactly which rule broke.
 */
class DocumentTest {

    /** Reusable valid values used by all the tests below. Fresh UUIDs on every test run. */
    private final DocumentId id = new DocumentId(UUID.randomUUID());

    private final TenantId tenantId = new TenantId(UUID.randomUUID());
    private final UserId ownerId = new UserId(UUID.randomUUID());
    private final Instant createdAt = Instant.parse("2026-07-16T10:00:00Z");

    /** Good input goes in, the same values come back out through the getters. */
    @Test
    void constructsWithValidFields() {
        Document doc = new Document(id, tenantId, ownerId, "Quarterly report", createdAt);
        assertThat(doc.id()).isEqualTo(id);
        assertThat(doc.tenantId()).isEqualTo(tenantId);
        assertThat(doc.ownerId()).isEqualTo(ownerId);
        assertThat(doc.title()).isEqualTo("Quarterly report");
        assertThat(doc.createdAt()).isEqualTo(createdAt);
    }

    /** A document must always have its own id. */
    @Test
    void rejectsNullId() {
        assertThatNullPointerException()
                .isThrownBy(() -> new Document(null, tenantId, ownerId, "t", createdAt));
    }

    /** A document must always belong to a tenant. */
    @Test
    void rejectsNullTenantId() {
        assertThatNullPointerException()
                .isThrownBy(() -> new Document(id, null, ownerId, "t", createdAt));
    }

    /** A document must always have an owner. */
    @Test
    void rejectsNullOwnerId() {
        assertThatNullPointerException()
                .isThrownBy(() -> new Document(id, tenantId, null, "t", createdAt));
    }

    /** A document must have a title (null is not allowed). */
    @Test
    void rejectsNullTitle() {
        assertThatNullPointerException()
                .isThrownBy(() -> new Document(id, tenantId, ownerId, null, createdAt));
    }

    /** A document must have a creation time. */
    @Test
    void rejectsNullCreatedAt() {
        assertThatNullPointerException()
                .isThrownBy(() -> new Document(id, tenantId, ownerId, "t", null));
    }

    /** A title made of only spaces counts as empty and is rejected. */
    @Test
    void rejectsBlankTitle() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Document(id, tenantId, ownerId, "   ", createdAt));
    }
}
