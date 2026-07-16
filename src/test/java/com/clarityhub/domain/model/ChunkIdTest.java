package com.clarityhub.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Checks that {@link ChunkId} rejects null and treats equal UUIDs as equal ids. */
class ChunkIdTest {

    /** A {@code ChunkId} without a UUID makes no sense — must throw. */
    @Test
    void rejectsNullValue() {
        assertThatNullPointerException().isThrownBy(() -> new ChunkId(null));
    }

    /** Two {@code ChunkId}s built from the same UUID are equal. */
    @Test
    void equalityByValue() {
        UUID uuid = UUID.randomUUID();
        assertThat(new ChunkId(uuid)).isEqualTo(new ChunkId(uuid));
    }
}
