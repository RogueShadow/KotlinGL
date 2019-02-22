package net.granseal.kotlinGL

import org.lwjgl.opengl.GL33.*

class ElementBufferObject(data: IntArray) {
    private val id = glGenBuffers()
    init{
        buffer(data)
    }
    fun bind(): ElementBufferObject {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, id)
        return this
    }
    fun buffer(data: IntArray): ElementBufferObject {
        bind()
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, data, GL_STATIC_DRAW)
        return this
    }
}