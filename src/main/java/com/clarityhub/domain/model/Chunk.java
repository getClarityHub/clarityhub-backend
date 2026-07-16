package com.clarityhub.domain.model;

import java.util.Objects;

/**
 * A small piece of a document, together with its embedding.
 *
 * <p>When a document is uploaded, we split its text into smaller pieces called chunks. Each chunk
 * gets its own embedding (a list of numbers describing its meaning — see {@link Embedding}).
 * Searching for text really means finding the chunks whose embeddings are closest in meaning to the
 * search query.
 *
 * <p>Permissions live on the document, not on individual chunks. A user who can read a document can
 * read all of its chunks.
 *
 * <p>Fields:
 *
 * <ul>
 *   <li>{@code id} — the chunk's own id.
 *   <li>{@code documentId} — the document this chunk was cut from.
 *   <li>{@code ordinal} — the chunk's position in the document, starting at 0. Lets us show chunks
 *       in the same order they appeared. Must be zero or positive.
 *   <li>{@code text} — the actual text of the chunk. Cannot be empty or only whitespace.
 *   <li>{@code embedding} — the numbers that describe what this chunk is about.
 * </ul>
 *
 * <p>Cannot change after creation. All the checks below run once, when the chunk is created.
 */
public record Chunk(
        ChunkId id, DocumentId documentId, int ordinal, String text, Embedding embedding) {

    /**
     * Rejects any missing field, negative ordinals, and blank text. If a chunk is created
     * successfully, the rest of the code can trust its data.
     */
    public Chunk {
        Objects.requireNonNull(id, "Chunk id must not be null");
        Objects.requireNonNull(documentId, "Chunk documentId must not be null");
        Objects.requireNonNull(text, "Chunk text must not be null");
        Objects.requireNonNull(embedding, "Chunk embedding must not be null");
        if (ordinal < 0) {
            throw new IllegalArgumentException(
                    "Chunk ordinal must be non-negative, was " + ordinal);
        }
        if (text.isBlank()) {
            throw new IllegalArgumentException("Chunk text must not be blank");
        }
    }
}
