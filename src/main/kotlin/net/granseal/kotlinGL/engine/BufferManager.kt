package net.granseal.kotlinGL.engine

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL33.*
import java.nio.IntBuffer

object BufferManager {
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
        val stride = mesh.combinedVerts.first().size * java.lang.Float.BYTES
        val vao = genAndBindVertexArray()
        val vbo = genAndBindBuffer(GL_ARRAY_BUFFER)
        var ebo = -1
        glBufferData(GL_ARRAY_BUFFER, mesh.combinedVerts.flatten().toFloatArray(), GL_STATIC_DRAW)
        if (mesh.isIndexed()){
            ebo = genAndBindBuffer(GL_ELEMENT_ARRAY_BUFFER)
            glBufferData(GL_ELEMENT_ARRAY_BUFFER,mesh.indices,GL_STATIC_DRAW)
        }

        if (mesh.normals.isNotEmpty() && mesh.textureCoords.isNotEmpty()) {
            setAttribPointer(0, 3, stride, 0)
            setAttribPointer(1, 3, stride, 3L * java.lang.Float.BYTES)
            setAttribPointer(2, 2, stride, 6L * java.lang.Float.BYTES)
        }
        if (mesh.normals.isNotEmpty() && mesh.textureCoords.isEmpty()){
            setAttribPointer(0, 3, stride, 0)
            setAttribPointer(1, 3, stride, 3L * java.lang.Float.BYTES)
        }
        if (mesh.normals.isEmpty() && mesh.textureCoords.isEmpty()){
            setAttribPointer(0,3,stride,0)
        }
        if (mesh.normals.isEmpty() && mesh.textureCoords.isNotEmpty()){
            setAttribPointer(0, 3, stride, 0)
            setAttribPointer(1, 2, stride, 3L * java.lang.Float.BYTES)
        }

        val buf = BufferUtils.createIntBuffer(mesh.indices.size)
        buf.put(mesh.indices)
        return VAO(vao,vbo,ebo,buf,0,mesh.verts.size,mesh.type)
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
            buf?.put(mesh.combinedVerts.flatten().toFloatArray().toByteBuffer())
            glUnmapBuffer(GL_ARRAY_BUFFER)
        }else{
            glBufferData(GL_ARRAY_BUFFER,mesh.combinedVerts.flatten().toFloatArray(), GL_STATIC_DRAW)
            mesh.vao = mesh.vao?.copy(endIndex = mesh.verts.size)
        }
    }

    fun draw(vao: VAO){
        if (vao.eboID != -1) {
            glBindVertexArray(vao.vaoID)
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,vao.eboID)
            glDrawElements(vao.type, vao.indices)
        }else{
            glBindVertexArray(vao.vaoID)
            glDrawArrays(vao.type,vao.startIndex,vao.endIndex)
        }
    }

    data class VAO(val vaoID: Int, val vboID: Int, val eboID: Int, val indices: IntBuffer, val startIndex: Int, val endIndex: Int, val type: Int = GL_TRIANGLES)
}

