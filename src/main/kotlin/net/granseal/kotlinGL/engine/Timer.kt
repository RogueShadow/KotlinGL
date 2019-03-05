package net.granseal.kotlinGL.engine

class Timer(val getTime: () -> Long) {
    var startTime = 0L
    var totalTime = 0L
    var lastMark = 0L
    var mode = 0 // 0 - seconds, 1 - milliseconds, 2 - microseconds, 3 - nanoseconds
    fun start(){
        startTime = getTime()
    }
    fun mark(): Float {
        lastMark = getTime() - startTime
        totalTime += lastMark
        startTime = getTime()
        return lastMark/1_000_000_000f
    }
    fun delta() = lastMark/1_000_000_000f

    fun timeElapsed() = totalTime/1_000_000_000f

    fun formatMark(): String {
        mark()
        return "${lastMark/div()}${unit()}. Total: ${totalTime/div()}${unit()}."
    }
    fun reset(){
        startTime = 0L
        totalTime = 0L
        lastMark = 0L
    }
    fun restart(){
        reset()
        start()
    }
    private fun div(): Double {
        return when (mode){
            0 -> 1_000_000_000.0
            1 -> 1_000_000.0
            2 -> 1_000.0
            else -> 1.0
        }
    }
    private fun unit(): String {
        return when (mode){
            0 -> "s"
            1 -> "ms"
            2 -> "us"
            else -> "ns"
        }
    }
}