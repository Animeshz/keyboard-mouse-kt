package com.github.animeshz.keyboard

import com.github.animeshz.keyboard.entity.Key
import com.github.animeshz.keyboard.entity.KeySet
import com.github.animeshz.keyboard.events.KeyEvent
import com.github.animeshz.keyboard.events.KeyState
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

/**
 * A typealias of lambda returned from [Keyboard.addShortcut] for better readability.
 */
public typealias Cancellable = () -> Unit

/**
 * Represents a keypress sequence with each element in ascending order of duration from the start time.
 */
@ExperimentalKeyIO
@ExperimentalTime
public typealias KeyPressSequence = List<Pair<Duration, KeyEvent>>

/**
 * The central class for receiving and interacting with the Keyboard Events.
 * This is wrapper around [NativeKeyboardHandler] providing high-level access to the Keyboard.
 *
 * The handlers are always invoked in a new coroutine, to let the [handler] emit the events quickly without any delay.
 * The [Exception] should be handled with the help of [CoroutineExceptionHandler] in the [CoroutineContext] provided.
 *
 * @param context The [CoroutineContext] used for processing of data.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
@ExperimentalKeyIO
@ExperimentalCoroutinesApi
public class Keyboard(
    context: CoroutineContext = Dispatchers.Default
) {
    private val scope = CoroutineScope(context + SupervisorJob())
    private var job: AtomicRef<Job?> = atomic(null)

    private val keyDownHandlers = atomic(mapOf<KeySet, suspend () -> Unit>())
    private val keyUpHandlers = atomic(mapOf<KeySet, suspend () -> Unit>())

    /**
     * The backing [NativeKeyboardHandler].
     */
    public val handler: NativeKeyboardHandler = nativeKbHandlerForPlatform()

    /**
     * Adds the [handler] to be invoked at [trigger] of the [keySet].
     *
     * @return Returns a [Cancellable], which when invoked the handler is removed.
     */
    public fun addShortcut(
        keySet: KeySet,
        trigger: KeyState = KeyState.KeyDown,
        handler: suspend () -> Unit
    ): Cancellable {
        val handlers = if (trigger == KeyState.KeyDown) keyDownHandlers else keyUpHandlers

        handlers.value += keySet to handler
        startIfNeeded()

        return {
            handlers.value -= keySet
            stopIfNeeded()
        }
    }

    /**
     * Presses and releases the [keySet] on the host machine.
     */
    public fun send(keySet: KeySet) {
        if (keySet.keys.isEmpty()) return

        for (key in keySet.keys) {
            handler.sendEvent(KeyEvent(key, KeyState.KeyDown))
        }

        for (key in keySet.keys) {
            handler.sendEvent(KeyEvent(key, KeyState.KeyUp))
        }
    }

    /**
     * Writes the following [string] on the host machine.
     */
    public fun write(string: String) {
        if (string.isEmpty()) return

        val capsState = handler.isCapsLockOn()
        for (char in string) {
            val (key, shift) = Key.fromChar(char)

            // Simplification of: char.toLowerCase() !in 'a'..'z' && shift || char.toLowerCase() in 'a'..'z' && shift != capsState
            if ((char.toLowerCase() in 'a'..'z' && capsState) != shift) {
                handler.sendEvent(KeyEvent(Key.LeftShift, KeyState.KeyDown))
                handler.sendEvent(KeyEvent(key, KeyState.KeyDown))
                handler.sendEvent(KeyEvent(key, KeyState.KeyUp))
                handler.sendEvent(KeyEvent(Key.LeftShift, KeyState.KeyUp))
            } else {
                handler.sendEvent(KeyEvent(key, KeyState.KeyDown))
                handler.sendEvent(KeyEvent(key, KeyState.KeyUp))
            }
        }
    }

    /**
     * Suspends till the [keySet] are pressed.
     */
    public suspend fun awaitTill(
        keySet: KeySet,
        trigger: KeyState = KeyState.KeyDown
    ): Unit = suspendCancellableCoroutine { cont ->
        val handlers = if (trigger == KeyState.KeyDown) keyDownHandlers else keyUpHandlers

        handlers.value += keySet to {
            handlers.value -= keySet
            stopIfNeeded()
            cont.resume(Unit)
        }

        startIfNeeded()

        cont.invokeOnCancellation {
            handlers.value -= keySet
            stopIfNeeded()
        }
    }

    /**
     * Records and returns a [KeyPressSequence] of all the keypress till a [keySet] is/are pressed.
     */
    @ExperimentalTime
    public suspend fun recordKeyPressesTill(
        keySet: KeySet,
        trigger: KeyState = KeyState.KeyDown
    ): KeyPressSequence = suspendCancellableCoroutine { cont ->
        val mark = TimeSource.Monotonic.markNow()
        val recording = atomic(true)

        val handlers = if (trigger == KeyState.KeyDown) keyDownHandlers else keyUpHandlers

        handlers.value += keySet to {
            recording.value = false
            handlers.value -= keySet
            stopIfNeeded()
        }
        startIfNeeded()

        val recJob = scope.launch {
            cont.resume(handler.events.map { mark.elapsedNow() to it }.takeWhile { recording.value }.toList())
        }

        cont.invokeOnCancellation {
            recJob.cancel()
            handlers.value -= keySet
            stopIfNeeded()
        }
    }

    /**
     * Plays the given [orderedPresses] with a speed of [speedFactor].
     */
    @ExperimentalTime
    public suspend fun play(orderedPresses: KeyPressSequence, speedFactor: Double = 1.0) {
        val mark = TimeSource.Monotonic.markNow()

        for ((duration, event) in orderedPresses) {
            delay((duration - mark.elapsedNow()) / speedFactor)
            handler.sendEvent(event)
        }
    }

    /**
     * Disposes this [Keyboard] instance.
     */
    public fun dispose() {
        scope.cancel(null)
        job.value = null
    }

    private fun startIfNeeded() {
        val jobCopy = job.value
        if (jobCopy != null && jobCopy.isActive) return

        job.value = scope.launch {
            val pressedKeys = mutableSetOf<Key>()

            handler.events.collect {
                if (it.state == KeyState.KeyDown) {
                    pressedKeys.add(it.key)
                    handleKeyDown(pressedKeys)
                } else {
                    handleKeyUp(pressedKeys)
                    pressedKeys.remove(it.key)
                }
            }
        }
    }

    private fun stopIfNeeded() {
        if (keyDownHandlers.value.count() != 0) return
        if (keyUpHandlers.value.count() != 0) return

        val activeJob = job.value ?: return
        activeJob.cancel()
        job.value = null
    }

    private fun handleKeyDown(pressedKeys: Set<Key>) {
        for ((keySet, handler) in keyDownHandlers.value) {
            if (pressedKeys.containsAll(keySet.keys)) {
                scope.launch { handler() }
                break
            }
        }
    }

    private fun handleKeyUp(pressedKeys: Set<Key>) {
        for ((keySet, handler) in keyUpHandlers.value) {
            if (pressedKeys.containsAll(keySet.keys)) {
                scope.launch { handler() }
                break
            }
        }
    }
}
