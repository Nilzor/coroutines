class TimedLog {
    init {
        val startTime = System.currentTimeMillis()
    }

    fun print(s: String) {
        System.out.println("${System.currentTimeMillis() - startTime} ms: $s")
    }
}