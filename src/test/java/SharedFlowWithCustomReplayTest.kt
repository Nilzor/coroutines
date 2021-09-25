import junit.framework.Assert.assertEquals
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.Test

class SharedFlowWithCustomReplayTest {
    private val threadPoolScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Test
    fun test() {
        val mainFlow = MutableSharedFlow<String>(10)

        runBlocking {
            mainFlow.emit("A")
            mainFlow.emit("B")
            mainFlow.emit("C")
            mainFlow.emit("D")
            mainFlow.emit("E")
        }

        val flowForTwo = mainFlow.shareIn(threadPoolScope, SharingStarted.Eagerly, 2)

        val collectedItems = mutableListOf<String>()
        ioScope.launch {
            flowForTwo.collect {
                collectedItems.add(it)
            }
        }
        Thread.sleep(100)
        assertEquals(2, collectedItems.size)
        assertEquals("D", collectedItems[0])
        assertEquals("E", collectedItems[1])
    }
}