package net.granseal.kotlinGL.engine

import org.lwjgl.opengl.GL33.*

object BufferManager {
    const val FLOAT_SIZE = 4

    val vaoList = mutableListOf<Int>()
    val buffers = mutableListOf<Int>()
    val fboList = mutableListOf<Int>()

    private fun genVertexArray(): Int {
        val vao = glGenVertexArrays()
        vaoList += vao
        return vao
    }
    private fun genBuffer(): Int {
        val buf = glGenBuffers()
        buffers += buf
        return buf
    }
    private fun genAndBindBuffer(type: Int): Int {
        val buf = genBuffer()
        glBindBuffer(type,buf)
        return buf
    }
    private fun genAndBindVertexArray(): Int {
        val vao = genVertexArray()
        glBindVertexArray(vao)
        return vao
    }
    fun genFrameBuffer(): Int {
        val fbo = glGenFramebuffers()
        fboList += fbo
        return fbo
    }

    fun createVAOFromMesh(mesh: Mesh): VAO {
        val vao = genAndBindVertexArray()
        val vbo = genAndBindBuffer(GL_ARRAY_BUFFER)
        val data = mesh.getCombinedFloatArray()
        glBufferData(GL_ARRAY_BUFFER, data, GL_DYNAMIC_DRAW)

        if (mesh.normals.isNotEmpty() && mesh.textureCoords.isNotEmpty()) {
            setAttribPointer(0, 3, 8 * FLOAT_SIZE, 0)
            setAttribPointer(1, 3, 8 * FLOAT_SIZE, 3L * FLOAT_SIZE)
            setAttribPointer(2, 2, 8 * FLOAT_SIZE, 6L * FLOAT_SIZE)
        }
        if (mesh.normals.isNotEmpty() && mesh.textureCoords.isEmpty()){
            setAttribPointer(0, 3, 6 * FLOAT_SIZE, 0)
            setAttribPointer(1, 3, 6 * FLOAT_SIZE, 3L * FLOAT_SIZE)
        }
        if (mesh.normals.isEmpty() && mesh.textureCoords.isEmpty()){
            setAttribPointer(0,3,3* FLOAT_SIZE,0)
        }
        if (mesh.normals.isEmpty() && mesh.textureCoords.isNotEmpty()){
            setAttribPointer(0, 3, 5 * FLOAT_SIZE, 0)
            setAttribPointer(1, 2, 5 * FLOAT_SIZE, 3L * FLOAT_SIZE)
        }

        return VAO(vao,vbo,0,mesh.verts.size/3,mesh.type)
    }

    fun cleanUp(){
        vaoList.forEach {
            glDeleteVertexArrays(it)
            println("Deleting VAO: $it")
        }
        buffers.forEach{
            glDeleteBuffers(it)
            println("Deleting Buffer: $it")
        }
        fboList.forEach {
            glDeleteFramebuffers(it)
            println("Deleting Frame Buffer: $it")
        }
        vaoList.clear()
        buffers.clear()
        fboList.clear()
    }

    private fun setAttribPointer(index: Int, size: Int, stride: Int, offset: Long){
        glVertexAttribPointer(index, size, GL_FLOAT, false, stride, offset)
        glEnableVertexAttribArray(index)
        println("Setting up AttribPointer Index: $index, size: $size, stride: $stride, offset: $offset")
    }

    fun updateVBO(mesh: Mesh, resize: Boolean = false) {
        val vao = mesh.vao
        glBindVertexArray(vao!!.vaoID)
        glBindBuffer(GL_ARRAY_BUFFER,vao.vboID)
        if (!resize) {
            //glBufferSubData(GL_ARRAY_BUFFER,0,mesh.getCombinedFloatArray())
            val buf = glMapBuffer(GL_ARRAY_BUFFER, GL_READ_WRITE)
            buf?.clear()
            buf?.put(mesh.getCombinedFloatArray().toByteBuffer())
            glUnmapBuffer(GL_ARRAY_BUFFER)
        }else{
            glBufferData(GL_ARRAY_BUFFER,mesh.getCombinedFloatArray(), GL_DYNAMIC_DRAW)
            mesh.vao = mesh.vao?.copy(endIndex = mesh.verts.size/3)
        }
    }
}

data class VAO(val vaoID: Int,val vboID: Int, val startIndex: Int, val endIndex: Int,val type: Int = GL_TRIANGLES) {
    fun draw() {
        glBindVertexArray(vaoID)
        glDrawArrays(type, startIndex, endIndex)
    }
}
