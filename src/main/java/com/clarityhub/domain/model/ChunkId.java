package com.clarityhub.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * The unique id of a chunk.
 *
 * <p>A chunk is a small piece of a document (see {@link Chunk}). One document contains many chunks,
 * so we need a separate id type to keep the two apart in code.
 *
 * <p>Cannot change after creation. Equality is by the UUID inside.
 */
public record ChunkId(UUID value) {

    /** Throws if you try to create a {@code ChunkId} with a null UUID. */
    public ChunkId {
        Objects.requireNonNull(value, "ChunkId value must not be null");
    }
}
