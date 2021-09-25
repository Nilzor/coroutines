import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

class Flow {
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

    fun emit(log: TimedLog): Flow<String> {
        return flow {
            log.print("emitting A")
            emit("A")
            delay(500)
            log.print("emitting B")
            emit ("B")
            delay (500)
            log.print("emitting C")
            emit ("C")
        }
    }
}