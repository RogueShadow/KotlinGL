package net.granseal.kotlinGL

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL33.*

class VertexArrayObject(val model: Model, val shader: ShaderProgram) {
    private val id = glGenVertexArrays()
    private var texID: Int? = null
    private var indexed: Boolean = false

    init {
        bind()
        val vbo = VertexBufferObject(model.getCombinedFloatArray())
        if (model.indices.isNotEmpty()) {
            val ebo = ElementBufferObject(model.indices)
            indexed = true
        }

        // Set Vertex Attribute Pointer
        glVertexAttribPointer(0, 3, GL_FLOAT, false, model.getStride(), 0)
        glEnableVertexAttribArray(0)
        if (model.normals != null){
            glVertexAttribPointer(1, 3, GL_FLOAT, false, model.getStride(),model.normOffset() )
            glEnableVertexAttribArray(1)
        }
        if (model.texEnabled()) {
            glVertexAttribPointer(2, 2, GL_FLOAT, false, model.getStride(), model.texOffset())
            glEnableVertexAttribArray(2)
        }
        if (model.textureFile.isNotEmpty()){
            texID = TextureLoader.loadGLTexture(model.textureFile)
        }
    }

    fun bind(): VertexArrayObject {
        glBindVertexArray(id)
        return this
    }

    fun draw() {
        bind()
        if (texID != null) glBindTexture(GL11.GL_TEXTURE_2D,texID!!)
        shader.use()
        shader.setUniform3f("objectColor",model.objectColor.x,model.objectColor.y,model.objectColor.z)
        if (indexed){
            glDrawElements(GL_TRIANGLES, model.indices.size, GL_UNSIGNED_INT, 0)
        }else{
            glDrawArrays(GL11.GL_TRIANGLES, 0, 36)
        }
    }
}