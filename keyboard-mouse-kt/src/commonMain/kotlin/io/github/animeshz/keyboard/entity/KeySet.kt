package io.github.animeshz.keyboard.entity

import io.github.animeshz.keyboard.ExperimentalKeyIO

/**
 * Represents an unordered set of [Key]s.
 */
@ExperimentalKeyIO
public class KeySet(
    public val keys: Set<Key>
) {
    public constructor(vararg keys: Key) : this(keys.toSet())

    public operator fun plus(other: KeySet): KeySet =
        KeySet(this.keys + other.keys)

    public operator fun plus(other: Key): KeySet =
        KeySet(this.keys + other)

    override fun toString(): String {
        return "KeySet(keys=$keys)"
    }

    override fun hashCode(): Int = keys.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other !is KeySet) return false

        return this.keys == other.keys
    }
}

/**
 * Creates a [KeySet] of two [Key]s.
 */
@ExperimentalKeyIO
public operator fun Key.plus(other: Key): KeySet =
    KeySet(setOf(this, other))
