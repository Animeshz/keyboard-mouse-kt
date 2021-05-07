package com.github.animeshz.keyboard

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal actual fun callAfter(duration: Duration, callback: () -> Unit) {
    Thread.sleep(duration.toLong(DurationUnit.MILLISECONDS))
    callback()
}
