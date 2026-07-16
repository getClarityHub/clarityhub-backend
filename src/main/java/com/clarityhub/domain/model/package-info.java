/**
 * The core business types: ids, documents, chunks, and embeddings.
 *
 * <p>Nothing in here talks to Spring, the database, or the web. That way we can test the rules with
 * plain Java, and if we ever change frameworks these classes still work as-is.
 *
 * <p>The rule is checked automatically by {@code ArchitectureTest} — the build fails if this
 * package starts importing framework code.
 */
package com.clarityhub.domain.model;
