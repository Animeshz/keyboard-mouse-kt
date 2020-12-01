package com.github.animeshz.globalhooks.keyboard

import com.github.animeshz.globalhooks.ExperimentalKeyIO
import com.github.animeshz.globalhooks.keyboard.entity.Key
import com.github.animeshz.globalhooks.keyboard.entity.KeySet
import com.github.animeshz.globalhooks.keyboard.events.KeyEventType
import com.github.animeshz.globalhooks.keyboard.internal.KBEventEmitter
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import mu.KotlinLogging

public typealias Cancellable = () -> Unit

private val logger = KotlinLogging.logger {}

@ExperimentalKeyIO
public class Keyboard(context: CoroutineContext = Dispatchers.Default) {
    private val scope = CoroutineScope(context + Job())
    private var job: Job? = null

    private val emitter = KBEventEmitter(scope)
    private val pressedKeys = mutableSetOf<Key>()
    private val keyDownHandlers = mutableMapOf<KeySet, suspend () -> Unit>()
    private val keyUpHandlers = mutableMapOf<KeySet, suspend () -> Unit>()

    /**
     * Adds the [handler] to be invoked at [trigger] of the [keySet].
     *
     * @return Returns a [Cancellable], which when invoked the handler is removed.
     */
    public fun on(
            keySet: KeySet,
            trigger: KeyEventType = KeyEventType.KeyDown,
            handler: suspend () -> Unit
    ): Cancellable {
        val handlers = if (trigger == KeyEventType.KeyDown) keyDownHandlers else keyUpHandlers

        handlers[keySet] = handler
        startIfNeeded()

        return {
            handlers.remove(keySet)
            stopIfNeeded()
        }
    }

    /**
     * Tries to press the [keys] on the host machine.
     * If successful returns true.
     */
    public suspend fun press(keys: KeySet): Boolean {
        TODO()
    }

    /**
     * Tries to write the following [string] on the host machine.
     */
    public suspend fun write(string: String) {
        TODO()
    }

    /**
     * Suspends till the [keySet] are pressed.
     */
    public suspend fun awaitTill(
            keySet: KeySet,
            trigger: KeyEventType = KeyEventType.KeyDown
    ): Unit = suspendCancellableCoroutine { cont ->
        val handlers = if (trigger == KeyEventType.KeyDown) keyDownHandlers else keyUpHandlers

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
     * Records and returns a [Flow] of all the keypress till a [keySet] is/are pressed.
     */
    @ExperimentalTime
    public suspend fun recordKeyPressesTill(
            keySet: KeySet,
            trigger: KeyEventType = KeyEventType.KeyDown
    ): List<Pair<Duration, Key>> = suspendCancellableCoroutine { cont ->
        val record = mutableListOf<Pair<Duration, Key>>()
        val mark = TimeSource.Monotonic.markNow()

        val handlers = if (trigger == KeyEventType.KeyDown) keyDownHandlers else keyUpHandlers

        val recJob = scope.launch {
            emitter.events.collect {
                record.add(mark.elapsedNow() to it.key)
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

    @ExperimentalTime
    public suspend fun play(orderedPresses: List<Pair<Duration, Key>>, speedFactor: Float = 1.0f) {
        TODO()
    }

    /**
     * Cancels all the [Job]s running under this Keyboard instance.
     */
    public fun cancel(cause: CancellationException? = null) {
        scope.cancel(cause)
    }

    private fun startIfNeeded() {
        if (job != null) return

        job = scope.launch {
            emitter.events.collect {
                when (it.type) {
                    KeyEventType.KeyDown -> {
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

    private suspend fun handleKeyDown() {
        for ((key, value) in keyDownHandlers) {
            if (handle(key, value)) return
        }
    }

    private suspend fun handleKeyUp() {
        for ((key, value) in keyUpHandlers) {
            if (handle(key, value)) return
        }
    }

    private suspend inline fun handle(keySet: KeySet, noinline handler: suspend () -> Unit): Boolean {
        if (!pressedKeys.containsAll(keySet.keys)) return false

        try {
            handler()
        } catch (e: Exception) {
            logger.error(e) { e.message }
        }
        return true
    }
}
