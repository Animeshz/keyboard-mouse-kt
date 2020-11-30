package com.github.animeshz.globalhooks.keyboard.entity

import com.github.animeshz.globalhooks.ExperimentalKeyIO

@Suppress("unused")
@ExperimentalKeyIO
public enum class Key(public val keyCode: Int) {
    // TODO("Add keys")
    ;

    override fun toString(): String {
        return "Key.$name(keyCode=$keyCode)"
    }
}
