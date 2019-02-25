package net.granseal.kotlinGL.engine

import net.granseal.kotlinGL.engine.BaseMesh.Companion.MESH_NORMALS
import net.granseal.kotlinGL.engine.BaseMesh.Companion.MESH_TEX
import net.granseal.kotlinGL.engine.BaseMesh.Companion.MESH_VERTS
import org.lwjgl.opengl.GL33
import kotlin.experimental.or

object VAOManager {
    const val FLOAT_SIZE = 4

    val vaoList = mutableListOf<Int>()
    val buffers = mutableListOf<Int>()

    private fun genVertexArray(): Int {
        val vao = GL33.glGenVertexArrays()
        vaoList += vao
        return vao
    }
    private fun genBuffer(): Int {
        val buf = GL33.glGenBuffers()
        buffers += buf
        return buf
    }
    private fun genAndBindBuffer(type: Int): Int {
        val buf = genBuffer()
        GL33.glBindBuffer(type,buf)
        return buf
    }
    private fun genAndBindVertexArray(): Int {
        val vao = genVertexArray()
        GL33.glBindVertexArray(vao)
        return vao
    }
    fun createVAOFromMesh(mesh: BaseMesh): VAO {
        val vao = genAndBindVertexArray()
        val vbo = genAndBindBuffer(GL33.GL_ARRAY_BUFFER)
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER,mesh.getCombinedFloatArray(),GL33.GL_STATIC_DRAW)

        when (mesh.getType()){
            MESH_VERTS or MESH_NORMALS -> {
                setAttribPointer(0,3,6 * FLOAT_SIZE,0)
                setAttribPointer(1,3,6 * FLOAT_SIZE,3)
            }
            MESH_VERTS or MESH_NORMALS or MESH_TEX -> {
                setAttribPointer(0,3,8 * FLOAT_SIZE,0)
                setAttribPointer(1,3,8 * FLOAT_SIZE,3L * FLOAT_SIZE)
                setAttribPointer(2,2,8 * FLOAT_SIZE,6L * FLOAT_SIZE)
            }
        }

        return VAO(vao,vbo)
    }

    fun cleanUp(){
        vaoList.forEach {
            GL33.glDeleteVertexArrays(it)
            println("Deleting VAO: $it")
        }
        buffers.forEach{
            GL33.glDeleteBuffers(it)
            println("Deleting Buffer: $it")
        }
        vaoList.clear()
        buffers.clear()
    }

    private fun setAttribPointer(index: Int, size: Int, stride: Int, offset: Long){
        GL33.glVertexAttribPointer(index, size, GL33.GL_FLOAT, false, stride, offset)
        GL33.glEnableVertexAttribArray(index)
        println("Setting up AttribPointer Index: $index, size: $size, stride: $stride, offset: $offset")
    }
}

class VAO(val vaoID: Int, val vboID: Int? = null) {
    fun bind() = GL33.glBindVertexArray(vaoID)
    fun draw(count: Int) {
        GL33.glBindVertexArray(vaoID)
        GL33.glDrawArrays(GL33.GL_TRIANGLES,0,count)
    }
}