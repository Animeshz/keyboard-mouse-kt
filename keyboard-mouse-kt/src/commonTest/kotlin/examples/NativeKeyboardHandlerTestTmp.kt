package examples

/**
 * Tests can be tried out after enabling granular source-set metadata in gradle.properties

import io.github.animeshz.kbms.entity.Key
import kotlin.test.Test
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking

/**
 * This is not really a Unit Test (since mocking is not available in Native),
 * but rather a real-time test (in other words you have to interact :p).
*/
@ExperimentalKeyIO
class NativeKeyboardHandlerTest {
@Test
fun `get state of Key`() = runBlocking {
val handler = nativeKbHandlerForPlatform()

delay(3000)  // To have a delay to check if KeyDown comes :P
println("State of Key A: ${handler.getKeyState(Key.A)}")
}

@Test
fun `get state of Caps Lock`() = runBlocking {
val handler = nativeKbHandlerForPlatform()

println("Toggle state of CapsLock: ${if (handler.isCapsLockOn()) "On" else "Off"}")
}

@Test
fun `listening to events`() = runBlocking {
val handler = nativeKbHandlerForPlatform()

println("Listening for first 5 events")
handler.events.take(5).collect { println(it) }
}
}
 */
