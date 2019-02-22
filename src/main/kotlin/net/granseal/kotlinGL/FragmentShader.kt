package net.granseal.kotlinGL

import org.lwjgl.opengl.GL33

class FragmentShader(source: String){
    val id = GL33.glCreateShader(GL33.GL_FRAGMENT_SHADER)
    init {
        GL33.glShaderSource(id, source)
        GL33.glCompileShader(id)
        val success = GL33.glGetShaderi(id, GL33.GL_COMPILE_STATUS)
        if (success != 1) println(GL33.glGetShaderInfoLog(id))
    }
}