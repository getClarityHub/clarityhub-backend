package com.clarityhub.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * The unique id of a tenant (a customer organization using ClarityHub).
 *
 * <p>Instead of passing raw {@link UUID} values around, we wrap them in a named type. That way the
 * compiler can tell a tenant id apart from a user id or a document id, and won't let you mix them
 * up by accident.
 *
 * <p>This class cannot change after it is created (immutable). Two {@code TenantId}s that hold the
 * same UUID count as equal.
 */
public record TenantId(UUID value) {

    /** Throws if you try to create a {@code TenantId} with a null UUID. */
    public TenantId {
        Objects.requireNonNull(value, "TenantId value must not be null");
    }
}
