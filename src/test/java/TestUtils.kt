/** Waits until a give condition is true. Checks every 100ms but times out after a given
 * timeout that is by default set to 10 seconds. Increase if network traffic is involved when waiting.
 */
fun waitUntil(timeoutSeconds: Long = 10, errorMessage: String? = null, condition: (() -> Boolean)) {
    val intervalMs = 100L
    val timeoutMs = timeoutSeconds * 1000L
    val timeStart = System.currentTimeMillis()
    do {
        if (condition.invoke()) return
        Thread.sleep(intervalMs)
        val timeSpent = System.currentTimeMillis() - timeStart
    } while (timeSpent < timeoutMs)
    var messageToPrint = "Timeout after $timeoutMs ms"
    if (errorMessage != null) messageToPrint = "$errorMessage - $messageToPrint"
    throw AssertionError(messageToPrint)
}