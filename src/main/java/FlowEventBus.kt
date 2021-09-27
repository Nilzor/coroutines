import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

/**
 * FlowEventBus where consumer can decide between single replay or no replay when collecting.
 * Warning: It has some concurrency issues that is apparent when you run the tests
 */
class FlowEventBus<T> {
    private val threadPoolScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val eventsWithSingleReplay = MutableSharedFlow<T>(replay = 1) // private mutable shared flow
    private val eventsWithoutReplay = eventsWithSingleReplay.shareIn(threadPoolScope, SharingStarted.Eagerly, replay = 0)

    val latest: T?
        get() = eventsWithSingleReplay.replayCache.lastOrNull()

    val subscriptionCount: Int
        get() = eventsWithSingleReplay.subscriptionCount.value

    /** Emit a new event */
    suspend fun emit(event: T) = eventsWithSingleReplay.emit(event)

    /**
     * Launches a coroutine on the specified [scope] and starts event collection.
     * Does not replay any events
     */
    fun collectOn(scope: CoroutineScope, action: suspend (value: T) -> Unit) {
        scope.launch {
            eventsWithoutReplay.collect(action)
        }
    }

    /** Consumers who wants the last event emitted as well as future events subscribe here */
    fun collectWithReplayOn(scope: CoroutineScope, action: suspend (value: T) -> Unit) {
        scope.launch {
            eventsWithSingleReplay.collect(action)
        }
    }
}