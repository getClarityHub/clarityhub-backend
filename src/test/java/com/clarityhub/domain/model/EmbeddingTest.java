package com.clarityhub.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import org.junit.jupiter.api.Test;

/**
 * Checks that {@link Embedding} behaves like a safe, unchangeable value:
 *
 * <ul>
 *   <li>bad input is rejected,
 *   <li>outside code cannot change the numbers inside,
 *   <li>equality is based on the numbers, not on object identity,
 *   <li>{@code toString} stays short.
 * </ul>
 */
class EmbeddingTest {

    /** Building an embedding with no numbers at all makes no sense. */
    @Test
    void rejectsNullVector() {
        assertThatNullPointerException().isThrownBy(() -> new Embedding(null));
    }

    /** An empty list of numbers can't be searched, so we reject it up front. */
    @Test
    void rejectsEmptyVector() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Embedding(new float[0]));
    }

    /** {@code dimensions()} should tell us how many numbers the embedding holds. */
    @Test
    void exposesDimensionality() {
        assertThat(new Embedding(new float[] {0.1f, 0.2f, 0.3f}).dimensions()).isEqualTo(3);
    }

    /**
     * If someone changes the array they gave us <b>after</b> creating the embedding, the embedding
     * must stay the same. Proves the constructor made its own copy.
     */
    @Test
    void defensivelyCopiesInputArray() {
        float[] source = {1.0f, 2.0f};
        Embedding embedding = new Embedding(source);
        source[0] = 99.0f;
        assertThat(embedding.vector()).containsExactly(1.0f, 2.0f);
    }

    /**
     * If someone changes the array returned by {@code vector()}, the embedding must stay the same.
     * Proves the getter also returns a copy.
     */
    @Test
    void defensivelyCopiesReturnedArray() {
        Embedding embedding = new Embedding(new float[] {1.0f, 2.0f});
        embedding.vector()[0] = 99.0f;
        assertThat(embedding.vector()).containsExactly(1.0f, 2.0f);
    }

    /**
     * Two embeddings with the same numbers should be equal <b>and</b> produce the same hash. Java's
     * built-in {@code HashMap} would break if equal objects had different hash codes.
     */
    @Test
    void equalityByVectorContents() {
        Embedding a = new Embedding(new float[] {0.1f, 0.2f});
        Embedding b = new Embedding(new float[] {0.1f, 0.2f});
        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
    }

    /** Different numbers means different embeddings. */
    @Test
    void inequalityForDifferentContents() {
        Embedding a = new Embedding(new float[] {0.1f, 0.2f});
        Embedding b = new Embedding(new float[] {0.1f, 0.3f});
        assertThat(a).isNotEqualTo(b);
    }

    /** {@code toString} must be short — never print the raw numbers. */
    @Test
    void toStringHidesRawVector() {
        assertThat(new Embedding(new float[] {0.1f, 0.2f, 0.3f}).toString())
                .isEqualTo("Embedding[dimensions=3]");
    }
}
