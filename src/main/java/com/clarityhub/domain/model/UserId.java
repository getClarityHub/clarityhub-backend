package com.clarityhub.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * The unique id of a user in the system.
 *
 * <p>Every user belongs to one tenant (see {@link TenantId}). We wrap the raw {@link UUID} in a
 * named type so a user id can't accidentally be passed where a different kind of id is expected.
 *
 * <p>Cannot change after creation. Equality is by the UUID inside.
 */
public record UserId(UUID value) {

    /** Throws if you try to create a {@code UserId} with a null UUID. */
    public UserId {
        Objects.requireNonNull(value, "UserId value must not be null");
    }
}
