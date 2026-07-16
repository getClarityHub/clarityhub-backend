package com.clarityhub.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * The unique id of a document.
 *
 * <p>A document is what users share and search inside. Permissions are attached to documents, so
 * whenever we ask "can this user see this thing?" we're really asking about a {@code DocumentId}.
 *
 * <p>Cannot change after creation. Equality is by the UUID inside.
 */
public record DocumentId(UUID value) {

    /** Throws if you try to create a {@code DocumentId} with a null UUID. */
    public DocumentId {
        Objects.requireNonNull(value, "DocumentId value must not be null");
    }
}
