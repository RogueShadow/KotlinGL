package net.granseal.kotlinGL.engine.math

import java.nio.FloatBuffer

/**
 * This class represents a 3x3-Matrix. GLSL equivalent to mat3.
 *
 * @author Heiko Brumme
 */
class Matrix3f {

    private var m00: Float = 0.toFloat()
    private var m01: Float = 0.toFloat()
    private var m02: Float = 0.toFloat()
    private var m10: Float = 0.toFloat()
    private var m11: Float = 0.toFloat()
    private var m12: Float = 0.toFloat()
    private var m20: Float = 0.toFloat()
    private var m21: Float = 0.toFloat()
    private var m22: Float = 0.toFloat()

    /**
     * Creates a 3x3 identity matrix.
     */
    constructor() {
        setIdentity()
    }

    /**
     * Creates a 3x3 matrix with specified columns.
     *
     * @param col1 Vector with values of the first column
     * @param col2 Vector with values of the second column
     * @param col3 Vector with values of the third column
     */
    constructor(col1: Vector3f, col2: Vector3f, col3: Vector3f) {
        m00 = col1.x
        m10 = col1.y
        m20 = col1.z

        m01 = col2.x
        m11 = col2.y
        m21 = col2.z

        m02 = col3.x
        m12 = col3.y
        m22 = col3.z
    }

    /**
     * Sets this matrix to the identity matrix.
     */
    fun setIdentity() {
        m00 = 1f
        m11 = 1f
        m22 = 1f

        m01 = 0f
        m02 = 0f
        m10 = 0f
        m12 = 0f
        m20 = 0f
        m21 = 0f
    }

    /**
     * Adds this matrix to another matrix.
     *
     * @param other The other matrix
     *
     * @return Sum of this + other
     */
    operator fun plus(other: Matrix3f): Matrix3f {
        val result = Matrix3f()

        result.m00 = this.m00 + other.m00
        result.m10 = this.m10 + other.m10
        result.m20 = this.m20 + other.m20

        result.m01 = this.m01 + other.m01
        result.m11 = this.m11 + other.m11
        result.m21 = this.m21 + other.m21

        result.m02 = this.m02 + other.m02
        result.m12 = this.m12 + other.m12
        result.m22 = this.m22 + other.m22

        return result
    }

    /**
     * Negates this matrix.
     *
     * @return Negated matrix
     */
    fun negate(): Matrix3f {
        return this * (-1f)
    }

    /**
     * Subtracts this matrix from another matrix.
     *
     * @param other The other matrix
     *
     * @return Difference of this - other
     */
    fun subtract(other: Matrix3f): Matrix3f {
        return this + other.negate()
    }

    /**
     * Multiplies this matrix with a scalar.
     *
     * @param scalar The scalar
     *
     * @return Scalar product of this * scalar
     */
    operator fun times(scalar: Float): Matrix3f {
        val result = Matrix3f()

        result.m00 = this.m00 * scalar
        result.m10 = this.m10 * scalar
        result.m20 = this.m20 * scalar

        result.m01 = this.m01 * scalar
        result.m11 = this.m11 * scalar
        result.m21 = this.m21 * scalar

        result.m02 = this.m02 * scalar
        result.m12 = this.m12 * scalar
        result.m22 = this.m22 * scalar

        return result
    }

    /**
     * Multiplies this matrix to a vector.
     *
     * @param vector The vector
     *
     * @return Vector product of this * other
     */
    operator fun times(vector: Vector3f): Vector3f {
        val x = this.m00 * vector.x + this.m01 * vector.y + this.m02 * vector.z
        val y = this.m10 * vector.x + this.m11 * vector.y + this.m12 * vector.z
        val z = this.m20 * vector.x + this.m21 * vector.y + this.m22 * vector.z
        return Vector3f(x, y, z)
    }

    /**
     * Multiplies this matrix to another matrix.
     *
     * @param other The other matrix
     *
     * @return Matrix product of this * other
     */
    operator fun times(other: Matrix3f): Matrix3f {
        val result = Matrix3f()

        result.m00 = this.m00 * other.m00 + this.m01 * other.m10 + this.m02 * other.m20
        result.m10 = this.m10 * other.m00 + this.m11 * other.m10 + this.m12 * other.m20
        result.m20 = this.m20 * other.m00 + this.m21 * other.m10 + this.m22 * other.m20

        result.m01 = this.m00 * other.m01 + this.m01 * other.m11 + this.m02 * other.m21
        result.m11 = this.m10 * other.m01 + this.m11 * other.m11 + this.m12 * other.m21
        result.m21 = this.m20 * other.m01 + this.m21 * other.m11 + this.m22 * other.m21

        result.m02 = this.m00 * other.m02 + this.m01 * other.m12 + this.m02 * other.m22
        result.m12 = this.m10 * other.m02 + this.m11 * other.m12 + this.m12 * other.m22
        result.m22 = this.m20 * other.m02 + this.m21 * other.m12 + this.m22 * other.m22

        return result
    }

    /**
     * Transposes this matrix.
     *
     * @return Transposed matrix
     */
    fun transpose(): Matrix3f {
        val result = Matrix3f()

        result.m00 = this.m00
        result.m10 = this.m01
        result.m20 = this.m02

        result.m01 = this.m10
        result.m11 = this.m11
        result.m21 = this.m12

        result.m02 = this.m20
        result.m12 = this.m21
        result.m22 = this.m22

        return result
    }

    /**
     * Stores the matrix in a given Buffer.
     *
     * @param buffer The buffer to store the matrix data
     */
    fun toBuffer(buffer: FloatBuffer) {
        buffer.put(m00).put(m10).put(m20)
        buffer.put(m01).put(m11).put(m21)
        buffer.put(m02).put(m12).put(m22)
        buffer.flip()
    }


}