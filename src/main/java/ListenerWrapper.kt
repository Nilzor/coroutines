import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

open class ListenerWrapper {
    interface MyCallback {
        fun onDone()
    }

    suspend fun listenerWrapper() {

        val target = Target()
        val job = setupListeners(target)
        run(target)
        val result = job.await()
        println("GOT RESULT: $result")
    }

    fun setupListenersWithSuspend(target: Target): Deferred<String> {
        println("setupListeners() bofore GlobalScope.async")
        val deferred = GlobalScope.async<String> {
            println("setupListeners() before suspendCoroutine")
            suspendCoroutine<String> {
                println("setupListeners() after suspendCoroutine")
                target.listener = object : MyCallback {
                    override fun onDone() {
                        println("DONE!")
                        it.resume("DONE!")
                    }
                }
            }
        }
        return deferred
    }

    fun setupListeners(target: Target): Deferred<String> {
        println("setupListeners() bofore GlobalScope.async")
        val deffered = CompletableDeferred<String>()

        target.listener = object : MyCallback {
            override fun onDone() {
                println("DONE!")
                deffered.complete("DONE!")
            }
        }
        println("Listener ready")
        return deffered
    }

    suspend fun run(target: Target) {
        println("RUN!")
        delay(100)
        target.listener?.onDone()
    }

    class Target {
        var listener: MyCallback? = null
    }

}