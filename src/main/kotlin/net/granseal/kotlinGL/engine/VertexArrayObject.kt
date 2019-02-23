package net.granseal.kotlinGL.engine

import net.granseal.kotlinGL.engine.shaders.ShaderProgram
import org.lwjgl.opengl.GL33.*

class VertexArrayObject(val model: Model, val shader: ShaderProgram) {
    private val id = glGenVertexArrays()
    private var vbo: Int
    private var ebo: Int? = null
    private var texID: Int? = null
    private var indexed: Boolean = false

    init {
        glBindVertexArray(id)
        vbo = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER,vbo)
        glBufferData(GL_ARRAY_BUFFER,model.getCombinedFloatArray(), GL_STATIC_DRAW)
        if (model.indices.isNotEmpty()) {
            ebo = glGenBuffers()
            if (ebo != null)glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,ebo!!)else throw Exception("Didn't generate EBO buffer")
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, model.indices, GL_STATIC_DRAW)
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

    fun draw() {
        glBindVertexArray(id)
        if (texID != null) glBindTexture(GL_TEXTURE_2D,texID!!)
        shader.use()
        shader.setUniform3f("objectColor",model.objectColor.x,model.objectColor.y,model.objectColor.z)
        if (indexed){
            glDrawElements(GL_TRIANGLES, model.indices.size, GL_UNSIGNED_INT, 0)
        }else{
            glDrawArrays(GL_TRIANGLES, 0, model.verticies.size/3)
        }
    }
}