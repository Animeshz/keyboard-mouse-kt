@file:Suppress("NON_EXPORTABLE_TYPE")

package io.github.animeshz.keyboard

import co.touchlab.stately.isolate.IsolateState
import io.github.animeshz.keyboard.entity.Key
import io.github.animeshz.keyboard.entity.KeySet
import io.github.animeshz.keyboard.entity.KeyState
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

// https://youtrack.jetbrains.com/issue/KT-45434
@ExperimentalJsExport
@JsExport
public fun interface ShortcutHandler {
    public fun handle(id: Int)
}

// https://youtrack.jetbrains.com/issue/KT-45434
@ExperimentalKeyIO
@ExperimentalTime
@ExperimentalJsExport
@JsExport
public fun interface KeyPressRecordCallback {
    public fun handle(seq: KeyPressSequence)
}

/**
 * Represents a keypress sequence with each element in ascending order of duration from the start time.
 */
@ExperimentalKeyIO
@ExperimentalTime
public typealias KeyPressSequence = Array<Triple<Duration, Key, KeyState>>

/**
 * The central class for receiving and interacting with the Keyboard Events.
 * This is wrapper around [NativeKeyboard] providing high-level access to the Keyboard.
 */
@ExperimentalJsExport
@Suppress("unused", "MemberVisibilityCanBePrivate")
@ExperimentalKeyIO
@JsExport
public object Keyboard {
    private val keyDownHandlers = IsolateState { mutableMapOf<Int, Pair<KeySet, ShortcutHandler>>() }
    private val keyUpHandlers = IsolateState { mutableMapOf<Int, Pair<KeySet, ShortcutHandler>>() }
    private val handlerId = AtomicInt(-1)

    private val idCount = AtomicInt(0)

    /**
     * The backing [NativeKeyboard].
     */
    public val native: NativeKeyboard = NativeKeyboard

    /**
     * Adds the [handler] to be invoked at [trigger] of the [keySet].
     *
     * @return Returns unique id which can be used to cancel subscription.
     */
    public fun addShortcut(
        keySet: KeySet,
        trigger: KeyState = KeyState.KeyDown,
        handler: ShortcutHandler
    ): Int {
        val handlers = if (trigger == KeyState.KeyDown) keyDownHandlers else keyUpHandlers

        handlers.access { it[idCount.value++] = Pair(keySet, handler) }
        startIfNeeded()

        return idCount.value
    }

    public fun removeShortcut(
        id: Int,
        trigger: KeyState = KeyState.KeyDown
    ) {
        val handlers = if (trigger == KeyState.KeyDown) keyDownHandlers else keyUpHandlers

        handlers.access { it.remove(id) }
        stopIfNeeded()
    }

    /**
     * Presses and releases the [keySet] on the host machine.
     */
    public fun send(keySet: KeySet) {
        if (keySet.keys.isEmpty()) return

        for (key in keySet.keys) {
            native.sendEvent(key.keyCode, true)
        }

        for (key in keySet.keys) {
            native.sendEvent(key.keyCode, false)
        }
    }

    /**
     * Writes the following [string] on the host machine.
     */
    public fun write(string: String) {
        if (string.isEmpty()) return

        val capsState = native.isCapsLockOn()
        for (char in string) {
            val (key, shift) = Key.fromChar(char)

            // Simplification of: char.toLowerCase() !in 'a'..'z' && shift || char.toLowerCase() in 'a'..'z' && shift != capsState
            if ((char.toLowerCase() in 'a'..'z' && capsState) != shift) {
                native.sendEvent(Key.LeftShift.keyCode, true)
                native.sendEvent(key.keyCode, true)
                native.sendEvent(key.keyCode, false)
                native.sendEvent(Key.LeftShift.keyCode, false)
            } else {
                native.sendEvent(key.keyCode, true)
                native.sendEvent(key.keyCode, false)
            }
        }
    }

    /**
     * Suspends till the [keySet] are pressed.
     */
    public fun triggerWhen(
        keySet: KeySet,
        trigger: KeyState = KeyState.KeyDown,
        handler: ShortcutHandler
    ) {
        val handlers = if (trigger == KeyState.KeyDown) keyDownHandlers else keyUpHandlers

        handlers.access {
            it[idCount.value++] = Pair(keySet, ShortcutHandler { id ->
                handlers.access { h -> h.remove(id) }
                stopIfNeeded()
                handler.handle(id)
            })
        }

        startIfNeeded()
    }

    /**
     * Records and returns a [KeyPressSequence] of all the keypress till a [keySet] is/are pressed.
     */
    @ExperimentalTime
    public fun recordKeyPressesTill(
        keySet: KeySet,
        trigger: KeyState = KeyState.KeyDown,
        handler: KeyPressRecordCallback
    ) {
        val handlers = if (trigger == KeyState.KeyDown) keyDownHandlers else keyUpHandlers
        val mark = TimeSource.Monotonic.markNow()
        var recording = true

        handlers.access {
            it[idCount.value++] = Pair(keySet, ShortcutHandler { id ->
                recording = false
                handlers.access { h -> h.remove(id) }
                stopIfNeeded()
            })
        }

        startIfNeeded()

        val seq: MutableList<Triple<Duration, Key, KeyState>> = mutableListOf()
        native.addEventHandler { id, key, isPressed ->
            if (!recording) {
                native.removeEventHandler(id)
                handler.handle(seq.toTypedArray())
            }

            val state = if (isPressed) KeyState.KeyDown else KeyState.KeyUp
            seq.add(Triple(mark.elapsedNow(), Key.fromKeyCode(key), state))
        }
    }

    /**
     * Plays the given [orderedPresses] with a speed of [speedFactor].
     *
     * In JVM and native targets it blocks, whereas in JS it enqueues task to the event loop.
     */
    @ExperimentalTime
    public fun play(orderedPresses: KeyPressSequence, speedFactor: Double = 1.0) {
        val mark = TimeSource.Monotonic.markNow()

        for ((duration, key, pressed) in orderedPresses) {
            callAfter((duration - mark.elapsedNow()) / speedFactor) {
                native.sendEvent(key.keyCode, pressed == KeyState.KeyDown)
            }
        }
    }

    private fun startIfNeeded() {
        if (handlerId.value != -1) return

        val pressedKeys = mutableSetOf<Key>()

        handlerId.value = native.addEventHandler { _, keyCode, isPressed ->
            if (isPressed) {
                pressedKeys.add(Key.fromKeyCode(keyCode))
                handleKeyDown(pressedKeys)
            } else {
                handleKeyUp(pressedKeys)
                pressedKeys.remove(Key.fromKeyCode(keyCode))
            }
        }
    }

    private fun stopIfNeeded() {
        if (keyDownHandlers.access { it.count() } != 0) return
        if (keyUpHandlers.access { it.count() } != 0) return

        val id = handlerId.value
        if (id != -1) { native.removeEventHandler(id) }
        handlerId.value = -1
    }

    private fun handleKeyDown(pressedKeys: Set<Key>) {
        keyDownHandlers.access {
            for ((id, pair) in it) {
                val (keySet, handler) = pair

                if (pressedKeys.containsAll(keySet.keys)) {
                    handler.handle(id)
                    break
                }
            }
        }
    }

    private fun handleKeyUp(pressedKeys: Set<Key>) {
        keyUpHandlers.access {
            for ((id, pair) in it) {
                val (keySet, handler) = pair

                if (pressedKeys.containsAll(keySet.keys)) {
                    handler.handle(id)
                    break
                }
            }
        }
    }
}

/**
 * In JVM and native targets it blocks, whereas in JS it enqueues task to the event loop.
 */
@ExperimentalTime
internal expect fun callAfter(duration: Duration, callback: () -> Unit)

public expect class AtomicInt(value_: Int) {
    public var value: Int
}
