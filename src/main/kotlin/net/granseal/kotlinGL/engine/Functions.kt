package net.granseal.kotlinGL.engine

import com.curiouscreature.kotlin.math.Float3
import java.nio.ByteBuffer
import kotlin.random.Random


fun getMesh(name: String) = Data.getMesh(name)
fun getTex(name: String) = Data.getTex(name)
fun loadResourceFile(f: String) = Data.loadResourceFile(f)
fun FloatArray.toByteBuffer(): ByteBuffer {
    val buffer = ByteBuffer.allocateDirect(this.size * java.lang.Float.BYTES)
    this.forEach { buffer.putFloat(it) }
    return buffer
}
val rand = Random(System.nanoTime())
fun rColor() = Float3(rand.nextFloat(), rand.nextFloat(), rand.nextFloat())