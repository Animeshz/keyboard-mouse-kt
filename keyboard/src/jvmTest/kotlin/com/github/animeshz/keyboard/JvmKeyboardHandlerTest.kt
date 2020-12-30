package com.github.animeshz.keyboard

import io.kotest.core.spec.style.StringSpec

@ExperimentalKeyIO
class JvmKeyboardHandlerTest : StringSpec({

    "JvmKeyboardHandlerNativeUtilTest" {
        println(JvmKeyboardHandler.isCapsLockOn())
    }

})
