package net.granseal.kotlinGL.engine

/*
 * The MIT License (MIT)
 *
 * Copyright © 2015-2017, Heiko Brumme
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


import java.nio.FloatBuffer

/**
 * This class represents a (x,y,z)-Vector. GLSL equivalent to vec3.
 *
 * @author Heiko Brumme
 */
class Vector3f {

    var x: Float = 0.toFloat()
    var y: Float = 0.toFloat()
    var z: Float = 0.toFloat()

    /**
     * Creates a default 3-tuple vector with all values set to 0.
     */
    constructor() {
        this.x = 0f
        this.y = 0f
        this.z = 0f
    }

    /**
     * Creates a 3-tuple vector with specified values.
     *
     * @param x x value
     * @param y y value
     * @param z z value
     */
    constructor(x: Float, y: Float, z: Float) {
        this.x = x
        this.y = y
        this.z = z
    }

    /**
     * Calculates the squared length of the vector.
     *
     * @return Squared length of this vector
     */
    fun lengthSquared(): Float {
        return x * x + y * y + z * z
    }

    /**
     * Calculates the length of the vector.
     *
     * @return Length of this vector
     */
    fun length(): Float {
        return Math.sqrt(lengthSquared().toDouble()).toFloat()
    }

    /**
     * Normalizes the vector.
     *
     * @return Normalized vector
     */
    fun normalize(): Vector3f {
        val len = length()
        return if (len > 0){
            this / length()
        }else {
            this
        }
    }

    /**
     * Adds this vector to another vector.
     *
     * @param other The other vector
     *
     * @return Sum of this + other
     */
    operator fun plus(other: Vector3f): Vector3f {
        val x = this.x + other.x
        val y = this.y + other.y
        val z = this.z + other.z
        return Vector3f(x, y, z)
    }

    /**
     * Negates this vector.
     *
     * @return Negated vector
     */
    fun negate() = scale(-1f)

    /**
     * Subtracts this vector from another vector.
     *
     * @param other The other vector
     *
     * @return Difference of this - other
     */
    operator fun minus(other: Vector3f) = this + other.negate()

    /**
     * Multiplies a vector by a scalar.
     *
     * @param scalar Scalar to multiply
     *
     * @return Scalar product of this * scalar
     */
    infix fun scale(scalar: Float): Vector3f {
        val x = this.x * scalar
        val y = this.y * scalar
        val z = this.z * scalar
        return Vector3f(x, y, z)
    }

    /**
     * Divides a vector by a scalar.
     *
     * @param scalar Scalar to multiply
     *
     * @return Scalar quotient of this / scalar
     */
    operator fun div(scalar: Float) = scale(1f / scalar)

    /**
     * Calculates the dot product of this vector with another vector.
     *
     * @param other The other vector
     *
     * @return Dot product of this * other
     */
    operator fun times(other: Vector3f): Float {
        return this.x * other.x + this.y * other.y + this.z * other.z
    }

    /**
     * Calculates the dot product of this vector with another vector.
     *
     * @param other The other vector
     *
     * @return Cross product of this x other
     */
    infix fun cross(other: Vector3f): Vector3f {
        val x = this.y * other.z - this.z * other.y
        val y = this.z * other.x - this.x * other.z
        val z = this.x * other.y - this.y * other.x
        return Vector3f(x, y, z)
    }

    /**
     * Calculates a linear interpolation between this vector with another
     * vector.
     *
     * @param other The other vector
     * @param alpha The alpha value, must be between 0.0 and 1.0
     *
     * @return Linear interpolated vector
     */
    fun lerp(other: Vector3f, alpha: Float): Vector3f {
        return this.scale(1f - alpha) + (other.scale(alpha))
    }

    /**
     * Stores the vector in a given Buffer.
     *
     * @param buffer The buffer to store the vector data
     */
    fun toBuffer(buffer: FloatBuffer) {
        buffer.put(x).put(y).put(z)
        buffer.flip()
    }

    override fun toString(): String {
        return "Vector3f($x,$y,$z)"
    }

}