package net.granseal.kotlinGL.engine

import java.nio.ByteBuffer


fun getMesh(name: String) = Data.getMesh(name)
fun getTex(name: String) = Data.getTex(name)
fun loadResourceFile(f: String) = Data.loadResourceFile(f)
fun FloatArray.toByteBuffer(): ByteBuffer {
    val buffer = ByteBuffer.allocateDirect(this.size * java.lang.Float.BYTES)
    this.forEach { buffer.putFloat(it) }
    return buffer
}