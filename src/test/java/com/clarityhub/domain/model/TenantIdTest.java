package com.clarityhub.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Checks that {@link TenantId} rejects bad input and treats two ids with the same UUID as equal.
 */
class TenantIdTest {

    /** You should not be able to build a {@code TenantId} without a real UUID. */
    @Test
    void rejectsNullValue() {
        assertThatNullPointerException().isThrownBy(() -> new TenantId(null));
    }

    /** Whatever UUID you put in, {@code value()} gives you the same UUID back. */
    @Test
    void exposesUnderlyingUuid() {
        UUID uuid = UUID.randomUUID();
        assertThat(new TenantId(uuid).value()).isEqualTo(uuid);
    }

    /** Two {@code TenantId}s built from the same UUID are considered equal. */
    @Test
    void equalityByValue() {
        UUID uuid = UUID.randomUUID();
        assertThat(new TenantId(uuid)).isEqualTo(new TenantId(uuid));
    }
}
