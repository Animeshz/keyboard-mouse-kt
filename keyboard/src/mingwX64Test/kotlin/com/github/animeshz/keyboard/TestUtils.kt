package com.github.animeshz.keyboard

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext

//@Suppress("EXPERIMENTAL_API_USAGE")
actual val testCoroutineContext: CoroutineContext get() = TODO()
//    newSingleThreadContext("Linux Test Context")

actual fun runBlockingTest(block: suspend CoroutineScope.() -> Unit) =
    runBlocking { this.block() }
