/*
 * The MIT License (MIT)
 *
 * Copyright © 2014-2015, Heiko Brumme
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.granseal.kotlinGL

import org.lwjgl.glfw.GLFW.glfwGetTime

/**
 * The timer class is used for calculating delta time and also FPS and UPS
 * calculation.
 *
 * @author Heiko Brumme
 */
class Timer {

    /**
     * System time since last loop.
     */
    /**
     * Getter for the last loop time.
     *
     * @return System time of the last loop
     */
    var lastLoopTime: Double = 0.toDouble()
        private set
    /**
     * Used for FPS and UPS calculation.
     */
    private var timeCount: Float = 0.toFloat()
    /**
     * Frames per second.
     */
    private var fps: Int = 0
    /**
     * Counter for the FPS calculation.
     */
    private var fpsCount: Int = 0
    /**
     * Updates per second.
     */
    private var ups: Int = 0
    /**
     * Counter for the UPS calculation.
     */
    private var upsCount: Int = 0

    /**
     * Returns the time elapsed since `glfwInit()` in seconds.
     *
     * @return System time in seconds
     */
    val time: Double
        get() = glfwGetTime()

    /**
     * Returns the time that have passed since the last loop.
     *
     * @return Delta time in seconds
     */
    val delta: Float
        get() {
            val time = time
            val delta = (time - lastLoopTime).toFloat()
            lastLoopTime = time
            timeCount += delta
            return delta
        }

    /**
     * Initializes the timer.
     */
    fun init() {
        lastLoopTime = time
    }

    /**
     * Updates the FPS counter.
     */
    fun updateFPS() {
        fpsCount++
    }

    /**
     * Updates the UPS counter.
     */
    fun updateUPS() {
        upsCount++
    }

    /**
     * Updates FPS and UPS if a whole second has passed.
     */
    fun update() {
        if (timeCount > 1f) {
            fps = fpsCount
            fpsCount = 0

            ups = upsCount
            upsCount = 0

            timeCount -= 1f
        }
    }

    /**
     * Getter for the FPS.
     *
     * @return Frames per second
     */
    fun getFPS(): Int {
        return if (fps > 0) fps else fpsCount
    }

    /**
     * Getter for the UPS.
     *
     * @return Updates per second
     */
    fun getUPS(): Int {
        return if (ups > 0) ups else upsCount
    }

}