import kotlinx.coroutines.*

val ioScope = CoroutineScope(Dispatchers.IO)

fun main() {
    System.out.println("Yo")

    //launchManyBlockingIo()
    //Flow().emitAndCollect()
    //SharedFlowExample().emitAndCollect()
    GlobalScope.launch(Dispatchers.Unconfined) {
        ListenerWrapper().listenerWrapper()
    }
    Thread.sleep(200)
}

fun launchMadnyBlockingIo() {
    for (i in 1..100) {
        launchBlockingIo(i)
    }
    Thread.sleep(10000)
}

fun launchBlockingIo(i: Int) {
    ioScope.launch {
        runBlocking {
            System.out.println("$i START at $time ")
            delay(300)
            System.out.println("$i STOP  at $time")
        }
    }
}

val startTime = System.currentTimeMillis()
val time: String get() = (startTime - System.currentTimeMillis()).toString() + " ms"

