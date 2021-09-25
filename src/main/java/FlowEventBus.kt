import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.shareIn

class FlowEventBus<T> {
    private val threadPoolScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val eventsWithSingleReplay = MutableSharedFlow<T>(replay = 1) // private mutable shared flow
    private val eventsWithoutReplay = eventsWithSingleReplay.shareIn(threadPoolScope, SharingStarted.Eagerly, replay = 0)

    val latest: T?
        get() = eventsWithSingleReplay.replayCache.lastOrNull()

    val subscriptionCount: Int
        get() = eventsWithSingleReplay.subscriptionCount.value

    /** Emit a new event */
    suspend fun emit(event: T) = eventsWithSingleReplay.emit(event)

    /** Consumers who only wants events occuring from now on subscribe here */
    suspend fun collect(action: suspend (value: T) -> Unit) = eventsWithoutReplay.collect(action)

    /** Consumers who wants the last event emitted as well as future events subscribe here */
    suspend fun collectWithReplay(action: suspend (value: T) -> Unit) {
        eventsWithSingleReplay.collect(action)
    }
}