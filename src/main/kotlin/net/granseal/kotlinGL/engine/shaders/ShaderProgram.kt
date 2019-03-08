package net.granseal.kotlinGL.engine.shaders

import net.granseal.kotlinGL.engine.math.Matrix4f
import net.granseal.kotlinGL.engine.math.Vector3f
import org.lwjgl.opengl.GL33.*

class ShaderProgram(vert: String, frag: String){
    val id = glCreateProgram()
    init{
        val vid = VertexShader(vert).id
        val fid = FragmentShader(frag).id
        glAttachShader(id, vid)
        glAttachShader(id, fid)
        glLinkProgram(id)
        val success = glGetProgrami(id, GL_LINK_STATUS)
        if (success != 1){
            println(glGetProgramInfoLog(id))
            throw IllegalStateException("Shader failed to complile")
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
    fun setMat4(name: String, value: Matrix4f){
        use()
        glUniformMatrix4fv(glGetUniformLocation(id,name),false,value.toBuffer())
    }

    fun setUniform3f(name: String, v1: Float, v2: Float, v3: Float) {
        use()
        glUniform3f(glGetUniformLocation(id, name), v1, v2, v3)
    }

    fun setUniform1f(name: String, f1: Float) {
        use()
        glUniform1f(glGetUniformLocation(id,name),f1)
    }
}


class FragmentShader(source: String){
    val id = glCreateShader(GL_FRAGMENT_SHADER)
    init {
        glShaderSource(id, source)
        glCompileShader(id)
        val success = glGetShaderi(id, GL_COMPILE_STATUS)
        if (success != 1) println(glGetShaderInfoLog(id))
    }
}
class VertexShader(source: String){
    val id = glCreateShader(GL_VERTEX_SHADER)
    init {
        glShaderSource(id, source)
        glCompileShader(id)
        val success = glGetShaderi(id, GL_COMPILE_STATUS)
        if (success != 1) println(glGetShaderInfoLog(id))
    }
}