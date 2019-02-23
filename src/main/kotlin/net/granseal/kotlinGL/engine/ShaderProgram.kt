package net.granseal.kotlinGL.engine

import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL33.*

class ShaderProgram(vert: String, frag: String){
    private val id = glCreateProgram()
    init{
        val vid = VertexShader(vert).id
        val fid = FragmentShader(frag).id
        glAttachShader(id, vid)
        glAttachShader(id, fid)
        glLinkProgram(id)
        val success = glGetProgrami(id, GL_LINK_STATUS)
        if (success != 1){
            println(glGetProgramInfoLog(id))
            throw Exception("Shader failed to complile")
        }else{
            glDeleteShader(vid)
            glDeleteShader(fid)
        }
    }
    fun use(): ShaderProgram {
        glUseProgram(id)
        return this
    }

    fun setUniform4f(name: String, v1: Float, v2: Float, v3: Float, v4: Float){
        use()
        glUniform4f(glGetUniformLocation(id, name), v1, v2, v3, v4)
    }
    fun setBool(name: String, value: Boolean){
        use()
        glUniform1i(glGetUniformLocation(id, name), if (value) 1 else 0)
    }
    fun setInt(name: String, value: Int){
        use()
        glUniform1i(glGetUniformLocation(id, name), value)
    }
    fun setFloat(name: String, value: Float){
        use()
        glUniform1f(glGetUniformLocation(id, name), value)
    }
    fun setVec3(name: String, vec: Vector3f) = setUniform3f(name,vec.x,vec.y,vec.z)
    fun setUniformMat4(name: String, value: Matrix4f){
        use()
        GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(id,name),false,value.toBuffer())
    }

    fun setUniform3f(name: String, v1: Float, v2: Float, v3: Float) {
        use()
        glUniform3f(glGetUniformLocation(id, name), v1, v2, v3)
    }
    fun setMaterial(mat: Material){
        setInt("material.diffuse",mat.diffuse)
        setVec3("material.specular",mat.specular)
        setFloat("material.shininess",mat.shininess)
    }
    fun setLight(light: Light){
        setVec3("light.ambient",light.ambient)
        setVec3("light.diffuse",light.diffuse)
        setVec3("light.specular",light.specular)
    }
}

data class Material(val diffuse: Int,
                    val specular: Vector3f,
                    val shininess: Float){
    companion object {
        fun getDefaultMaterial(): Material {
            return Material(
                0,
                Vector3f(.5f, .5f, .5f),
                32f
            )
        }
    }
}

data class Light(val ambient: Vector3f,
                 val diffuse: Vector3f,
                 val specular: Vector3f
){
    companion object {
        fun getDefaultLight(): Light = Light(
            Vector3f(.1f, .1f, .1f),
            Vector3f(.5f, .5f, .5f),
            Vector3f(1f, 1f, 1f)
        )
    }
}