package com.github.animeshz.keyboard

import com.github.animeshz.keyboard.entity.Key
import com.github.animeshz.keyboard.entity.KeySet
import com.github.animeshz.keyboard.events.KeyEvent
import com.github.animeshz.keyboard.events.KeyState
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

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
public class Keyboard(
        context: CoroutineContext = Dispatchers.Default
) {
    private val scope = CoroutineScope(context + SupervisorJob())
    private var job: Job? = null

    private val pressedKeys = mutableSetOf<Key>()
    private val keyDownHandlers = mutableMapOf<KeySet, suspend () -> Unit>()
    private val keyUpHandlers = mutableMapOf<KeySet, suspend () -> Unit>()

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

        handlers[keySet] = handler
        startIfNeeded()

        return {
            handlers.remove(keySet)
            stopIfNeeded()
        }
    }

    /**
     * Presses the [keySet] on the host machine.
     */
    public fun press(keySet: KeySet) {
        if (keySet.keys.isEmpty()) return

        for (key in keySet.keys) {
            handler.sendEvent(KeyEvent(key, KeyState.KeyDown), moreOnTheWay = true)
        }

        val iterator = keySet.keys.iterator()
        while (iterator.hasNext()) {
            val key = iterator.next()
            handler.sendEvent(KeyEvent(key, KeyState.KeyUp), moreOnTheWay = iterator.hasNext())
        }
    }

    /**
     * Writes the following [string] on the host machine.
     */
    public fun write(string: String) {
        if (string.isEmpty()) return

        val iterator = string.iterator()
        while (iterator.hasNext()) {
            val char = iterator.next()
            val (key, shift) = Key.fromChar(char)

            if (shift) {
                handler.sendEvent(KeyEvent(Key.LeftShift, KeyState.KeyDown), moreOnTheWay = true)
                handler.sendEvent(KeyEvent(key, KeyState.KeyDown), moreOnTheWay = true)
                handler.sendEvent(KeyEvent(key, KeyState.KeyUp), moreOnTheWay = true)
                handler.sendEvent(KeyEvent(Key.LeftShift, KeyState.KeyUp), moreOnTheWay = iterator.hasNext())
            } else {
                handler.sendEvent(KeyEvent(key, KeyState.KeyDown), moreOnTheWay = true)
                handler.sendEvent(KeyEvent(key, KeyState.KeyUp), moreOnTheWay = iterator.hasNext())
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

        handlers[keySet] = {
            handlers.remove(keySet)
            stopIfNeeded()
            cont.resume(Unit)
        }

        startIfNeeded()

        cont.invokeOnCancellation {
            handlers.remove(keySet)
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
        val record = mutableListOf<Pair<Duration, KeyEvent>>()
        val mark = TimeSource.Monotonic.markNow()

        val handlers = if (trigger == KeyState.KeyDown) keyDownHandlers else keyUpHandlers

        val recJob = scope.launch {
            handler.events.collect {
                record.add(mark.elapsedNow() to it)
            }
        }
        handlers[keySet] = {
            handlers.remove(keySet)
            recJob.cancel()
            stopIfNeeded()
            cont.resume(record)
        }

        startIfNeeded()

        cont.invokeOnCancellation {
            handlers.remove(keySet)
            recJob.cancel()
            stopIfNeeded()
        }
    }

    /**
     * Plays the given [orderedPresses] with a speed of [speedFactor].
     */
    @ExperimentalTime
    public suspend fun play(orderedPresses: KeyPressSequence, speedFactor: Double = 1.0) {
        val mark = TimeSource.Monotonic.markNow()

        val iterator = orderedPresses.iterator()
        while (iterator.hasNext()) {
            val (duration, event) = iterator.next()
            delay((duration - mark.elapsedNow()) / speedFactor)

            handler.sendEvent(event, moreOnTheWay = iterator.hasNext())
        }
    }

    /**
     * Cancels all the [Job]s running under this Keyboard instance.
     */
    public fun cancel(cause: CancellationException? = null) {
        scope.cancel(cause)
    }

    private fun startIfNeeded() {
        if (job != null) return
        if (job!!.isActive) return

        job = scope.launch {
            handler.events.collect {
                when (it.state) {
                    KeyState.KeyDown -> {
                        pressedKeys.add(it.key)
                        handleKeyDown()
                    }
                    else -> {
                        handleKeyUp()
                        pressedKeys.remove(it.key)
                    }
                }
            }
        }
    }

    private fun stopIfNeeded() {
        if (keyDownHandlers.count() != 0) return
        if (keyUpHandlers.count() != 0) return

        val activeJob = job ?: return
        activeJob.cancel()
        job = null
    }

    private fun handleKeyDown() {
        for ((keySet, handler) in keyDownHandlers) {
            if (pressedKeys.containsAll(keySet.keys)) {
                scope.launch { handler() }
                return
            }
        }
    }

    private fun handleKeyUp() {
        for ((keySet, handler) in keyUpHandlers) {
            if (pressedKeys.containsAll(keySet.keys)) {
                scope.launch { handler() }
                return
            }
        }
    }
}
