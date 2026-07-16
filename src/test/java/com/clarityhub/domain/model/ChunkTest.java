package com.clarityhub.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Checks that {@link Chunk} accepts valid input and rejects every kind of invalid input. One test
 * per rule so a red test tells you exactly which rule broke.
 */
class ChunkTest {

    /** Reusable valid values shared by the tests below. Fresh UUIDs on every test run. */
    private final ChunkId id = new ChunkId(UUID.randomUUID());

    private final DocumentId documentId = new DocumentId(UUID.randomUUID());
    private final Embedding embedding = new Embedding(new float[] {0.1f, 0.2f});

    /** Good input goes in, the same values come back out through the getters. */
    @Test
    void constructsWithValidFields() {
        Chunk chunk = new Chunk(id, documentId, 0, "hello", embedding);
        assertThat(chunk.id()).isEqualTo(id);
        assertThat(chunk.documentId()).isEqualTo(documentId);
        assertThat(chunk.ordinal()).isZero();
        assertThat(chunk.text()).isEqualTo("hello");
        assertThat(chunk.embedding()).isEqualTo(embedding);
    }

    /** A chunk must always have its own id. */
    @Test
    void rejectsNullId() {
        assertThatNullPointerException()
                .isThrownBy(() -> new Chunk(null, documentId, 0, "t", embedding));
    }

    /** A chunk must always know which document it came from. */
    @Test
    void rejectsNullDocumentId() {
        assertThatNullPointerException().isThrownBy(() -> new Chunk(id, null, 0, "t", embedding));
    }

    /** A chunk must have text (null is not allowed). */
    @Test
    void rejectsNullText() {
        assertThatNullPointerException()
                .isThrownBy(() -> new Chunk(id, documentId, 0, null, embedding));
    }

    /** A chunk must have an embedding, otherwise it can never be found in search. */
    @Test
    void rejectsNullEmbedding() {
        assertThatNullPointerException().isThrownBy(() -> new Chunk(id, documentId, 0, "t", null));
    }

    /** Whitespace-only text counts as empty and is rejected. */
    @Test
    void rejectsBlankText() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Chunk(id, documentId, 0, " ", embedding));
    }

    /** Chunk positions start at 0 and go up; a negative position is a bug and rejected. */
    @Test
    void rejectsNegativeOrdinal() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Chunk(id, documentId, -1, "t", embedding));
    }
}
