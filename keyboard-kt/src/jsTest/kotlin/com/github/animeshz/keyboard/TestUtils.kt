package com.github.animeshz.keyboard

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise

actual fun runBlockingTest(block: suspend CoroutineScope.() -> Unit): dynamic =
    GlobalScope.promise { this.block() }
