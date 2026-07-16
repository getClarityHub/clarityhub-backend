package com.clarityhub.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Checks that {@link UserId} rejects null and treats equal UUIDs as equal ids. Same idea as {@link
 * TenantIdTest} but kept separate so a failing test names the exact type.
 */
class UserIdTest {

    /** A {@code UserId} without a UUID makes no sense — must throw. */
    @Test
    void rejectsNullValue() {
        assertThatNullPointerException().isThrownBy(() -> new UserId(null));
    }

    /** Two {@code UserId}s built from the same UUID are equal. */
    @Test
    void equalityByValue() {
        UUID uuid = UUID.randomUUID();
        assertThat(new UserId(uuid)).isEqualTo(new UserId(uuid));
    }
}
