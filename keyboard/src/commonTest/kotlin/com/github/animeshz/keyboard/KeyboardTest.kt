package com.github.animeshz.keyboard

/**
 * Tests can be tried out after enabling granular source-set metadata in gradle.properties

import com.github.animeshz.keyboard.entity.Key
import com.github.animeshz.keyboard.entity.KeySet
import com.github.animeshz.keyboard.entity.plus
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * This is not really a Unit Test (since mocking is not available in Native),
 * but rather a real-time test (in other words you have to interact :p).
*/
@ExperimentalKeyIO
class KeyboardTest {
private lateinit var keyboard: Keyboard

@BeforeTest
fun before() {
keyboard = Keyboard()
}

@AfterTest
fun after() {
keyboard.cancel()
}

@Test
fun `awaitTill should wait till specific key combination is matched`() = runBlocking {
println("Waiting for LeftCtrl + LeftAlt + O")

keyboard.awaitTill(Key.LeftCtrl + Key.LeftAlt + Key.O)
println("OK :)")
}

@Test
fun `addShortcut should add hotkey and be triggered on every keypress`() = runBlocking {
println("Waiting for LeftAlt + Y for 3 times")

val counter = atomic(0)
val lock = Mutex(true)
val canceller = keyboard.addShortcut(Key.LeftAlt + Key.Y) {
if (counter.incrementAndGet() >= 3) lock.unlock()
}

lock.withLock {
canceller()
println("OK :)")
}
}

@Test
fun `write should write the given string into the host`() = runBlocking {
val toWrite = "Hello Keyboard!"

println("Press Enter in any application (even this console) to write 'Hello Keyboard!' on it.")
keyboard.awaitTill(KeySet(Key.Enter))
keyboard.write(toWrite)
}

@ExperimentalTime
@Test
fun `record test`() = runBlocking {
println("Recording KeyPresses till LeftCtrl + S is pressed")

val record = keyboard.recordKeyPressesTill(Key.LeftCtrl + Key.S)
println(record.map { it.second })
}

@ExperimentalTime
@Test
fun `play test`() = runBlocking {
println("Press Enter to record KeyPresses")
keyboard.awaitTill(KeySet(Key.Enter))

println("Recording KeyPresses till LeftCtrl + S is pressed")
val record = keyboard.recordKeyPressesTill(Key.LeftCtrl + Key.S)

print("Playing recorded events at speed of 1.75x")
keyboard.play(record, 1.75)
}
}
 */
