package net.granseal.kotlinGL.engine

class Timer {
    var startTime = 0L
    var totalTime = 0L
    var lastMark = 0L

    fun reset(){
        startTime = 0L
        totalTime = 0L
        lastMark = 0L
    }
    fun start(){
        startTime = System.nanoTime()
    }
    fun restart(){
        reset()
        start()
    }
    fun mark(): Float {
        lastMark = System.nanoTime() - startTime
        totalTime += lastMark
        startTime = System.nanoTime()
        return lastMark.toSeconds()
    }
    fun formatMark(): String {
        mark()
        return "${lastMark.toSeconds()}s. Total: ${totalTime.toSeconds()}s."
    }
    fun delta() = lastMark.toSeconds()
    fun timeElapsed() = totalTime.toSeconds()
    private fun Long.toSeconds(): Float = this/1_000_000_000f
}