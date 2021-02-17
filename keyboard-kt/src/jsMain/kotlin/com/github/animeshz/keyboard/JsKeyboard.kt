package com.github.animeshz.keyboard

import com.github.animeshz.keyboard.entity.KeySet
import com.github.animeshz.keyboard.events.KeyEvent
import com.github.animeshz.keyboard.events.KeyState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlin.js.Promise
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

@ExperimentalJsExport
@ExperimentalTime
@JsExport
@JsName("KeyPressSequence")
public class TimedKeyEvent(
    public val durationInSeconds: Double,
    public val key: String,
    public val isPressed: Boolean
)

@Suppress("unused")
@ExperimentalJsExport
@ExperimentalKeyIO
@ExperimentalCoroutinesApi
@JsExport
@JsName("JsKeyboard")
public class JsKeyboard {
    private val delegate = Keyboard()

    @JsName("handler")
    public val handler: JsNativeKeyboardHandler = JsNativeKeyboardHandler

    private fun parseKeySet(str: String): KeySet =
        str.split(Regex("""\s*\+\s*"""))
            .asSequence()
            .map { it.toKey() }
            .toSet()
            .let { KeySet(it) }

    /**
     * Adds the [handler] to be invoked at either on press or release defined by [triggerOnPressed] of the [keySet].
     *
     * @return Returns a Cancellable [kotlin.reflect.KFunction], which when invoked the handler is removed.
     */
    @JsName("addShortcut")
    public fun addShortcut(
        keySet: String,
        triggerOnPressed: Boolean,
        handler: () -> Unit
    ): Cancellable {
        return delegate.addShortcut(parseKeySet(keySet), triggerOnPressed.toKeyState()) { handler() }
    }

    /**
     * Presses and releases the [keySet] on the host machine.
     */
    @JsName("send")
    public fun send(keySet: String) {
        delegate.send(parseKeySet(keySet))
    }

    /**
     * Writes the following [string] on the host machine.
     */
    @JsName("write")
    public fun write(string: String) {
        delegate.write(string)
    }

    /**
     * Returns a [Promise] that notifies for completion when [keySet] are pressed.
     */
    @JsName("completeWhenPressed")
    public fun completeWhenPressed(
        keySet: String,
        triggerOnPressed: Boolean = true
    ): Promise<Nothing?> =
        GlobalScope.promise {
            delegate.awaitTill(parseKeySet(keySet), triggerOnPressed.toKeyState())
            null
        }

    /**
     * Records and returns a [KeyPressSequence] of all the keypress till a [keySet] is/are pressed.
     */
    @ExperimentalTime
    @JsName("recordKeyPressesTill")
    public fun recordKeyPressesTill(
        keySet: String,
        triggerOnPressed: Boolean = true
    ): Promise<Array<TimedKeyEvent>> =
        GlobalScope.promise {
            delegate.recordKeyPressesTill(parseKeySet(keySet), triggerOnPressed.toKeyState())
                .map { TimedKeyEvent(it.first.inSeconds, it.second.key.name, it.second.state == KeyState.KeyDown) }
                .toTypedArray()
        }

    /**
     * Plays the given [orderedPresses] with a speed of [speedFactor].
     *
     * @return A [Promise] for subscribing to get notified when does play finishes.
     */
    @ExperimentalTime
    @JsName("play")
    public fun play(orderedPresses: Array<TimedKeyEvent>, speedFactor: Double = 1.0): Promise<Nothing?> =
        GlobalScope.promise {
            val sequence = orderedPresses.map { it.durationInSeconds.seconds to KeyEvent(it.key.toKey(), it.isPressed.toKeyState()) }
            delegate.play(sequence, speedFactor)
            null
        }

    /**
     * Disposes this [Keyboard] instance.
     */
    @JsName("dispose")
    public fun dispose() {
        delegate.dispose()
    }
}
