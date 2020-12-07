package com.github.animeshz.keyboard.entity

import com.github.animeshz.keyboard.ExperimentalKeyIO

@ExperimentalKeyIO
public class KeySet(
        public val keys: Set<Key>
) {
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

@ExperimentalKeyIO
public operator fun Key.plus(other: Key): KeySet =
        KeySet(setOf(this, other))
