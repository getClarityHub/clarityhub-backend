package com.clarityhub.domain.model;

import java.util.Arrays;
import java.util.Objects;

/**
 * A list of numbers that represents the meaning of a piece of text.
 *
 * <p>An AI model turns text into these numbers. Texts with similar meaning end up with similar
 * numbers, which is how we can search by meaning instead of exact keywords.
 *
 * <p>This class is a bit more complicated than the other value types because Java arrays are
 * tricky:
 *
 * <ul>
 *   <li>Arrays can be changed after they are handed over. We <b>copy</b> the input when creating
 *       the {@code Embedding}, and copy again when handing it out, so nobody can change our
 *       internal numbers from the outside.
 *   <li>Two arrays with the same numbers are <b>not</b> considered equal by Java by default. We fix
 *       that by writing our own {@code equals} and {@code hashCode} that compare the numbers.
 *   <li>Printing an array with 1500+ numbers would flood the logs, so {@code toString} only shows
 *       the size.
 * </ul>
 */
public record Embedding(float[] vector) {

    /** Rejects null and empty input, then stores a copy so outside changes can't affect us. */
    public Embedding {
        Objects.requireNonNull(vector, "Embedding vector must not be null");
        if (vector.length == 0) {
            throw new IllegalArgumentException("Embedding vector must not be empty");
        }
        vector = vector.clone();
    }

    /** Returns a fresh copy of the numbers so callers cannot change our stored array. */
    @Override
    public float[] vector() {
        return vector.clone();
    }

    /** How many numbers this embedding has. All embeddings from the same model share this size. */
    public int dimensions() {
        return vector.length;
    }

    /** Two embeddings are equal when every number in the same position matches. */
    @Override
    public boolean equals(Object other) {
        return other instanceof Embedding that && Arrays.equals(this.vector, that.vector);
    }

    /** Hash code based on the numbers, so it matches how {@link #equals(Object)} works. */
    @Override
    public int hashCode() {
        return Arrays.hashCode(vector);
    }

    /** Short text form for logs. Never dump the raw numbers — they can be huge and noisy. */
    @Override
    public String toString() {
        return "Embedding[dimensions=" + vector.length + "]";
    }
}
