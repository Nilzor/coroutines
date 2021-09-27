import junit.framework.TestCase
import kotlinx.coroutines.*

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import java.lang.RuntimeException


class FlowEventBusTest {

    @Test
    fun flowWithReplay_replaysLastEvent() {
        val bus = getInstance()
        val event1 = "A"
        val event2 = "B"
        runBlocking {
            bus.emit(event1)
            bus.emit(event2)
            assertThat(bus.latest, `is`(event2))
        }

        Thread.sleep(50)
        var recievedEvent: String? = null
        bus.collectWithReplayOn(CoroutineScope(Dispatchers.Unconfined)) {
            recievedEvent = it
            // cancel() // ..or the coroutine will continue forever waiting for more to collect
        }

        Thread.sleep(50)
        assertThat(recievedEvent, `is`(event2))
    }

    @Test
    fun flowWithoutReplay_shouldOnlyGetItemsEmittedAfterCollectCalled_loop100() {
        val time = System.currentTimeMillis()
        for (i in 1..100) {
            flowWithoutReplay_shouldOnlyGetItemsEmittedAfterCollectCalled()
            log("  -- " + (System.currentTimeMillis() - time))
        }
    }

    @Test
    fun flowWithoutReplay_shouldOnlyGetItemsEmittedAfterCollectCalled() {
        val bus = getInstance()
        val event1 = "A"
        val event2 = "B"
        runBlocking {
            log( "Emitting $event1 to ${bus.subscriptionCount} subscribers")
            bus.emit(event1)
        }
        val collectedItems = mutableListOf<String>()
        log("Collecting... (sub count: ${bus.subscriptionCount})")
        bus.collectOn(ioScope) {
            collectedItems.add(it)
        }
        waitUntil { bus.subscriptionCount > 0 }
        runBlocking {
            log( "Emitting $event2 to ${bus.subscriptionCount} subscribers")
            bus.emit(event2)
        }
        waitUntil(2) {
            collectedItems.size > 0
        }
        assertThat(collectedItems[0], `is`(event2))
    }

    @Test
    fun flowWithoutReplay_shouldNotGetPreviouslyEmittedItem() {
        val bus = getInstance()
        val event1 = "A"
        runBlocking { bus.emit(event1) }
        val collectedItems = mutableListOf<String>()
        bus.collectOn(ioScope) {
            collectedItems.add(it)
        }
        Thread.sleep(200)
        assertThat(collectedItems.size, equalTo(0))
    }

    @Test
    fun exceptionInCollectCoroutine() {
        val bus = getInstance()
        val collectedItems1 = mutableListOf<String>()
        val collectedItems2 = mutableListOf<String>()
        bus.collectOn(ioScope) {
            collectedItems1.add(it)
            throw RuntimeException("Sorry!")
        }
        bus.collectOn(ioScope) {
            collectedItems2.add(it)
        }
        Thread.sleep(50)
        runBlocking {
            bus.emit("A")
            bus.emit("B")
        }

        Thread.sleep(50)
        assertThat(collectedItems1.size, equalTo(1))
        assertThat(collectedItems2.size, equalTo(2))
    }

    @Test
    fun latest_containsLatestValue() {
        val bus = getInstance()
        runBlocking { bus.emit("A") }
        runBlocking { bus.emit("B") }
        assertThat(bus.latest, equalTo("B"))
    }

    private fun getInstance(): FlowEventBus<String> {
        return FlowEventBus()
    }

    private fun log(s: String) = System.out.println(s)
}