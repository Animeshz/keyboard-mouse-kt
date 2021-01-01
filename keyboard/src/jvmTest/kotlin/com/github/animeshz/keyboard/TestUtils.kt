package com.github.animeshz.keyboard

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking

actual fun runBlockingTest(block: suspend CoroutineScope.() -> Unit) =
    runBlocking { this.block() }
