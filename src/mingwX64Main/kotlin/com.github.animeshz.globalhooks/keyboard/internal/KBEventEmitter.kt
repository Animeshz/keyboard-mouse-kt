package com.github.animeshz.globalhooks.keyboard.internal

import com.github.animeshz.globalhooks.ExperimentalKeyIO
import com.github.animeshz.globalhooks.keyboard.events.KeyEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow

@ExperimentalKeyIO
internal actual class KBEventEmitter actual constructor(val scope: CoroutineScope) {
    actual val events: SharedFlow<KeyEvent>
        get() = TODO("Not yet implemented")
}
