package com.github.animeshz.globalhooks.keyboard.internal

import com.github.animeshz.globalhooks.ExperimentalKeyIO
import com.github.animeshz.globalhooks.keyboard.events.KeyEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow

@ExperimentalKeyIO
internal expect class KBEventEmitter(scope: CoroutineScope) {
    val events: SharedFlow<KeyEvent>
}
