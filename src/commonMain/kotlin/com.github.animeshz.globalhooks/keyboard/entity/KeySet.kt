package com.github.animeshz.globalhooks.keyboard.entity

import com.github.animeshz.globalhooks.ExperimentalKeyIO

@ExperimentalKeyIO
class KeySet(
        val keys: Set<Key>
) {
    operator fun plus(other: KeySet): KeySet =
            KeySet(this.keys + other.keys)

    operator fun plus(other: Key): KeySet =
            KeySet(this.keys + other)

    override fun hashCode(): Int = keys.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other !is KeySet) return false

        return this.keys == other.keys
    }
}

@ExperimentalKeyIO
operator fun Key.plus(other: Key): KeySet =
        KeySet(setOf(this, other))
