import junit.framework.TestCase

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test


class FlowEventBusTest {

    @Test(expected = CancellationException::class)
    fun flowWithReplay_replaysLastEvent() {
        val bus = getInstance()
        val event1 = "A"
        val event2 = "B"
        runBlocking {
            bus.emit(event1)
            bus.emit(event2)
            assertThat(bus.latest, `is`(event2))
            bus.collectWithReplay {
                assertThat(it, `is`(event2))
                cancel() // ..or the coroutine will continue forever waiting for more to collect
            }
        }
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
        ioScope.launch {
            log("Collecting... (sub count: ${bus.subscriptionCount})")
            bus.collect {
                collectedItems.add(it)
            }
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
        ioScope.launch {
            bus.collect {
                collectedItems.add(it)
            }
        }
        Thread.sleep(200)
        assertThat(collectedItems.size, equalTo(0))
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