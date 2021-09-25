import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

class SharedFlowExample {
    fun emitAndCollect() {
        val log = TimedLog()
        runBlocking {
            val flow = emit(log)
            log.print("Before first sleep")
            delay(500)
            log.print("Starting collect")
            flow.collect {
                log.print("Collected: $it")
            }
            log.print("Collection done")
        }
    }

    fun emit(log: TimedLog): SharedFlow<String> {
        return flow {
            log.print("emitting A")
            emit("A")
            delay(500)
            log.print("emitting B")
            emit ("B")
            delay (500)
            log.print("emitting C")
            emit ("C")
        }.shareIn(
            CoroutineScope(Dispatchers.IO),
            SharingStarted.Eagerly,
            replay = 0
        )
    }
}