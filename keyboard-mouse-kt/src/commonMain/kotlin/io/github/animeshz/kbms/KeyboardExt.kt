@file:Suppress("NON_EXPORTABLE_TYPE")

package io.github.animeshz.kbms

import io.github.animeshz.kbms.entity.Key
import io.github.animeshz.kbms.entity.KeySet
import io.github.animeshz.kbms.entity.KeyState
import kotlin.jvm.JvmStatic
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

/**
 * Represents a keypress sequence with each element in ascending order of duration from the start time.
 */
@ExperimentalTime
public typealias KeyPressSequence = Array<Triple<Duration, Key, KeyState>>

/**
 * The central class for receiving and interacting with the Keyboard Events.
 * This is wrapper around [Keyboard] providing high-level access to the Keyboard.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
public object KeyboardExt {
    public class EventHandlerExt : Keyboard.EventHandler {
        public fun interface ShortcutHandler {
            public fun handle(keySet: KeySet): Unit
        }

        @ExperimentalTime
        public fun interface KeyPressRecordCallback {
            public fun handle(seq: KeyPressSequence)
        }

        private val pressedKeys: MutableSet<Key> = mutableSetOf()
        private val keyDownHandlers: MutableMap<KeySet, ShortcutHandler> = mutableMapOf()
        private val keyUpHandlers: MutableMap<KeySet, ShortcutHandler> = mutableMapOf()

        /**
         * Adds the [handler] to be invoked at [trigger] of the [keySet].
         *
         * @return Returns unique id which can be used to cancel subscription.
         */
        public fun addShortcut(
            keySet: KeySet,
            trigger: KeyState = KeyState.KeyDown,
            handler: ShortcutHandler
        ) {
            val handlers = if (trigger == KeyState.KeyDown) keyDownHandlers else keyUpHandlers
            handlers[keySet] = handler
        }

        public fun removeShortcut(
            keySet: KeySet,
            trigger: KeyState = KeyState.KeyDown
        ) {
            val handlers = if (trigger == KeyState.KeyDown) keyDownHandlers else keyUpHandlers
            handlers.remove(keySet)
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

            handlers[keySet] = ShortcutHandler {
                handlers.remove(it)
                handler.handle(it)
            }
        }

        /**
         * Records and returns a [KeyPressSequence] of all the keypress till a [keySet] is/are pressed.
         */
        @ExperimentalTime
        // TODO: Till a returned lambda call
        public fun recordKeyPressesTill(
            keySet: KeySet,
            trigger: KeyState = KeyState.KeyDown,
            handler: KeyPressRecordCallback
        ) {
            val handlers = if (trigger == KeyState.KeyDown) keyDownHandlers else keyUpHandlers
            val mark = TimeSource.Monotonic.markNow()
            var recording = true

            handlers[keySet] = ShortcutHandler {
                recording = false
                handlers.remove(it)
            }

            val seq: MutableList<Triple<Duration, Key, KeyState>> = mutableListOf()
            Keyboard.addEventHandler { key, isPressed ->
                if (!recording) {
                    Keyboard.removeEventHandler(this)
                    handler.handle(seq.toTypedArray())
                }

                val state = if (isPressed) KeyState.KeyDown else KeyState.KeyUp
                seq.add(Triple(mark.elapsedNow(), key, state))
            }
        }

        override fun handle(key: Key, isPressed: Boolean) {
            if (isPressed) {
                pressedKeys.add(key)
                handleKeyDown(pressedKeys)
            } else {
                handleKeyUp(pressedKeys)
                pressedKeys.remove(key)
            }
        }

        private fun handleKeyDown(pressedKeys: Set<Key>) {
            for ((keySet, handler) in keyDownHandlers) {
                if (pressedKeys.containsAll(keySet.keys)) {
                    handler.handle(keySet)
                    break
                }
            }
        }

        private fun handleKeyUp(pressedKeys: Set<Key>) {
            for ((keySet, handler) in keyUpHandlers) {
                if (pressedKeys.containsAll(keySet.keys)) {
                    handler.handle(keySet)
                    break
                }
            }
        }
    }

    /**
     * Presses and releases the [keySet] on the host machine.
     */
    @JvmStatic
    public fun send(keySet: KeySet) {
        if (keySet.keys.isEmpty()) return

        for (key in keySet.keys) {
            Keyboard.sendEvent(key, true)
        }

        for (key in keySet.keys) {
            Keyboard.sendEvent(key, false)
        }
    }

    /**
     * Writes the following [string] on the host machine.
     */
    @JvmStatic
    public fun write(string: String) {
        if (string.isEmpty()) return

        val capsState = Keyboard.isCapsLockOn()
        for (char in string) {
            val (key, shift) = Key.fromChar(char)

            // Simplification of: char.toLowerCase() !in 'a'..'z' && shift || char.toLowerCase() in 'a'..'z' && shift != capsState
            if ((char.lowercaseChar() in 'a'..'z' && capsState) != shift) {
                Keyboard.sendEvent(Key.LeftShift, true)
                Keyboard.sendEvent(key, true)
                Keyboard.sendEvent(key, false)
                Keyboard.sendEvent(Key.LeftShift, false)
            } else {
                Keyboard.sendEvent(key, true)
                Keyboard.sendEvent(key, false)
            }
        }
    }

    /**
     * Plays the given [orderedPresses] with a speed of [speedFactor].
     *
     * In JVM and native targets it blocks, whereas in JS it enqueues task to the event loop.
     */
    @JvmStatic
    @ExperimentalTime
    public fun play(orderedPresses: KeyPressSequence, speedFactor: Double = 1.0) {
        val mark = TimeSource.Monotonic.markNow()

        for ((duration, key, pressed) in orderedPresses) {
            callAfter((duration - mark.elapsedNow()) / speedFactor) {
                Keyboard.sendEvent(key, pressed == KeyState.KeyDown)
            }
        }
    }
}

/**
 * In JVM and native targets it blocks, whereas in JS it enqueues task to the event loop.
 */
@ExperimentalTime
internal expect fun callAfter(duration: Duration, callback: () -> Unit)
