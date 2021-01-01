package com.github.animeshz.keyboard

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

expect val testCoroutineContext: CoroutineContext
expect fun runBlockingTest(block: suspend CoroutineScope.() -> Unit)
