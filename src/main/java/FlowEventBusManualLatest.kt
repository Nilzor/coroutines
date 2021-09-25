import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect


/**
 * FlowEventBus with manual caching of "latest"
 */
open class FlowEventBusManualLatest<T> {

    private val events = MutableSharedFlow<T>(replay = 0) // private mutable shared flow

    var latest: T? = null
        private set

    val subscriptionCount: Int
        get() = events.subscriptionCount.value

    /** Emit a new event */
    suspend fun emit(event: T) {
        latest = event
        events.emit(event)  // suspends until all subscribers receive the event
    }

    /** Consumers who only wants events occuring from now on subscribe here */
    suspend fun collect(action: suspend (value: T) -> Unit) = events.collect(action)

    /** Consumers who wants the last event emitted as well as future events subscribe here */
    suspend fun collectWithReplay(action: suspend (value: T) -> Unit) {
        latest?.let { action(it) } // Replay any cached event
        events.collect(action)      // Listen for new events
    }
}

