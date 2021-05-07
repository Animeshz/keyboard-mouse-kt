@file:Suppress("NON_EXPORTABLE_TYPE")

package io.github.animeshz.keyboard

import io.github.animeshz.keyboard.entity.Key
import io.github.animeshz.keyboard.entity.KeySet
import io.github.animeshz.keyboard.entity.KeyState
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

private val byPlus = Regex("""\s*\+\s*""")

@ExperimentalJsExport
@ExperimentalKeyIO
@JsExport
public fun parseKey(str: String): Key = Key.values().first { it.name == str }

@ExperimentalJsExport
@ExperimentalKeyIO
@JsExport
public fun parseKeySet(str: String): KeySet =
    str.split(byPlus)
        .map { parseKey(it) }
        .toSet()
        .let { KeySet(it) }

@ExperimentalJsExport
@ExperimentalKeyIO
@JsExport
public fun parseKeyCode(keyCode: Int): Key = Key.fromKeyCode(keyCode)

@ExperimentalJsExport
@ExperimentalKeyIO
@JsExport
public fun onPressed(yes: Boolean): KeyState =
    if (yes) KeyState.KeyDown else KeyState.KeyUp

@ExperimentalTime
internal actual fun callAfter(duration: Duration, callback: () -> Unit) {
    setTimeout(callback, duration.toInt(DurationUnit.MILLISECONDS))
}
