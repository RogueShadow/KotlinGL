package net.granseal.kotlinGL.engine

import net.granseal.kotlinGL.engine.math.Matrix4f
import net.granseal.kotlinGL.engine.math.Vector3f
import net.granseal.kotlinGL.engine.shaders.Depth
import net.granseal.kotlinGL.engine.shaders.ShaderManager
import org.lwjgl.opengl.GL33
import org.lwjgl.system.MemoryUtil
import kotlin.properties.Delegates

object BufferManager {
    const val FLOAT_SIZE = 4

    val vaoList = mutableListOf<Int>()
    val buffers = mutableListOf<Int>()
    val fboList = mutableListOf<Int>()

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
    fun genFrameBuffer(): Int {
        val fbo = GL33.glGenFramebuffers()
        fboList += fbo
        return fbo
    }

    fun createVAOFromMesh(mesh: Mesh): VAO {
        val vao = genAndBindVertexArray()
        val vbo = genAndBindBuffer(GL33.GL_ARRAY_BUFFER)
        val data = mesh.getCombinedFloatArray()
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, data, GL33.GL_STATIC_DRAW)

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

        return VAO(vao,0,mesh.verts.size/3,mesh.type)
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
        fboList.forEach {
            GL33.glDeleteFramebuffers(it)
            println("Deleting Frame Buffer: $it")
        }
        vaoList.clear()
        buffers.clear()
        fboList.clear()
    }

    private fun setAttribPointer(index: Int, size: Int, stride: Int, offset: Long){
        GL33.glVertexAttribPointer(index, size, GL33.GL_FLOAT, false, stride, offset)
        GL33.glEnableVertexAttribArray(index)
        println("Setting up AttribPointer Index: $index, size: $size, stride: $stride, offset: $offset")
    }
}

class VAO(val vaoID: Int, val startIndex: Int, val endIndex: Int,val type: Int = GL33.GL_TRIANGLES) {
    fun draw() {
        GL33.glBindVertexArray(vaoID)
        GL33.glDrawArrays(type,startIndex,endIndex)
    }
}

class ShadowMap(val width: Int, val height: Int,val currentWidth: Int, val currentHeight: Int){
    var fboID: Int by Delegates.notNull()
    var texID: Int by Delegates.notNull()

    val shader = Depth(Matrix4f())

    fun generate() {
        fboID = BufferManager.genFrameBuffer()
        texID = TextureManager.genTexture()
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, texID)
        GL33.glTexImage2D(
            GL33.GL_TEXTURE_2D, 0, GL33.GL_DEPTH_COMPONENT, width, height,
            0, GL33.GL_DEPTH_COMPONENT, GL33.GL_FLOAT, 0L
        )

        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_NEAREST)
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_NEAREST)
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_BORDER)
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_BORDER)
        val borderColor = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)
        GL33.glTexParameterfv(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_BORDER_COLOR, borderColor)
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER,fboID)
        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER,GL33.GL_DEPTH_ATTACHMENT,GL33.GL_TEXTURE_2D, texID, 0)
        GL33.glDrawBuffer(GL33.GL_NONE)
        GL33.glReadBuffer(GL33.GL_NONE)
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0)
    }

    fun start(){
        GL33.glViewport(0,0,width,height)
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER,fboID)
            GL33.glClear(GL33.GL_DEPTH_BUFFER_BIT)
        val near = 1.0f
        val far = 7.5f
        val lightProj = Matrix4f.orthographic(-10f,10f,-10f,10f,near,far)
        val cam = Camera()
        cam.position(-2f,5f,-1f)
        cam.lookAt(Vector3f(0f,0f,0f))
        val lightView = cam.view
        val lsm = lightProj * lightView
        shader.lightSpaceMatrix = lsm
        GL33.glCullFace(GL33.GL_FRONT)
    }

    fun end(){
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER,0)
        GL33.glViewport(0,0,currentWidth,currentHeight)
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT or GL33.GL_DEPTH_BUFFER_BIT)
        //config shaders matrices
        GL33.glBindTexture(GL33.GL_TEXTURE_2D,texID)
        ShaderManager.setAllMat4("lightSpaceMatrix",shader.lightSpaceMatrix)
        ShaderManager.setAllInt("shadowMap",texID)
        GL33.glCullFace(GL33.GL_BACK)
    }
}