package net.granseal.kotlinGL.engine

import org.lwjgl.opengl.GL33.*

class VertexBufferObject(data: FloatArray) {
    private val id = glGenBuffers()
    init {
        buffer(data)
    }
    fun bind(): VertexBufferObject {
        glBindBuffer(GL_ARRAY_BUFFER, id)
        return this
    }
    fun buffer(data: FloatArray): VertexBufferObject {
        bind()
        glBufferData(GL_ARRAY_BUFFER,data, GL_STATIC_DRAW)
        return this
    }
}