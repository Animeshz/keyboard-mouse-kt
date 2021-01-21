package com.github.animeshz.keyboard

import com.github.animeshz.keyboard.entity.KeySet
import com.github.animeshz.keyboard.events.KeyEvent
import com.github.animeshz.keyboard.events.KeyState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

/**
 * The central class for receiving and interacting with the Keyboard Events.
 *
 * This is wrapper around [Keyboard], which is again a wrapper around [NativeKeyboardHandler].
 * However this offers a [handler] of type [JNativeKeyboardHandler] for easily interacting with
 * [NativeKeyboardHandler] using RxJava 3.
 */
@ExperimentalCoroutinesApi
@ExperimentalKeyIO
public class JKeyboard {
    private val delegate = Keyboard()

    /**
     * The RxJava 3 compatible form of backing [NativeKeyboardHandler].
     */
    public val handler: JNativeKeyboardHandler = JNativeKeyboardHandler

    /**
     * Adds the [handler] to be invoked at [trigger] of the [keySet].
     *
     * @return Returns a Cancellable [kotlin.reflect.KFunction], which when invoked the handler is removed.
     */
    @JvmOverloads
    public fun addShortcut(
        keySet: KeySet,
        trigger: KeyState = KeyState.KeyDown,
        handler: ShortcutHandler
    ): Cancellable {
        val kCancellable = delegate.addShortcut(keySet, trigger) { handler.handle() }

        return Cancellable { kCancellable() }
    }

    /**
     * Presses and releases the [keySet] on the host machine.
     */
    public fun send(keySet: KeySet) {
        delegate.send(keySet)
    }

    /**
     * Writes the following [string] on the host machine.
     */
    public fun write(string: String) {
        delegate.write(string)
    }

    /**
     * Returns a [CompletableFuture] that notifies for completion when [keySet] are pressed.
     */
    @JvmOverloads
    public fun completeWhenPressed(
        keySet: KeySet,
        trigger: KeyState = KeyState.KeyDown
    ): CompletableFuture<Unit> =
        GlobalScope.launch { delegate.awaitTill(keySet, trigger) }.asCompletableFuture()

    /**
     * Records and returns a [KeyPressSequence] of all the keypress till a [keySet] is/are pressed.
     */
    @ExperimentalTime
    @JvmOverloads
    public fun recordKeyPressesTill(
        keySet: KeySet,
        trigger: KeyState = KeyState.KeyDown
    ): CompletableFuture<List<Pair<Duration, KeyEvent>>> =
        GlobalScope.async { delegate.recordKeyPressesTill(keySet, trigger) }.asCompletableFuture()

    /**
     * Plays the given [orderedPresses] with a speed of [speedFactor].
     *
     * @return A [CompletableFuture] for subscribing to get notified when does play finishes.
     */
    @ExperimentalTime
    @JvmOverloads
    public fun play(orderedPresses: List<Pair<Duration, KeyEvent>>, speedFactor: Double = 1.0): CompletableFuture<Unit> =
        GlobalScope.launch { delegate.play(orderedPresses, speedFactor) }.asCompletableFuture()

    /**
     * Disposes this [Keyboard] instance.
     */
    public fun dispose() {
        delegate.dispose()
    }
}
