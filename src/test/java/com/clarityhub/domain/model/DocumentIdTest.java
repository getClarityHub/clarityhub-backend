package com.clarityhub.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Checks that {@link DocumentId} rejects null and treats equal UUIDs as equal ids. */
class DocumentIdTest {

    /** A {@code DocumentId} without a UUID makes no sense — must throw. */
    @Test
    void rejectsNullValue() {
        assertThatNullPointerException().isThrownBy(() -> new DocumentId(null));
    }

    /** Two {@code DocumentId}s built from the same UUID are equal. */
    @Test
    void equalityByValue() {
        UUID uuid = UUID.randomUUID();
        assertThat(new DocumentId(uuid)).isEqualTo(new DocumentId(uuid));
    }
}
