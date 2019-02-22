package net.granseal.kotlinGL

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL33.*
import org.lwjgl.stb.STBImage.stbi_load
import java.nio.ByteBuffer



object TextureLoader{

    internal data class Texture(val width: Int, val height: Int, val channels: Int, val data: ByteBuffer)

    private fun loadTexture(file: String): Texture {
        val w = BufferUtils.createIntBuffer(1)
        val h = BufferUtils.createIntBuffer(1)
        val c = BufferUtils.createIntBuffer(1)
        val data = stbi_load(file, w,h,c,0)
        if (data != null) {
            return Texture(w.get(), h.get(), c.get(), data)
        }else throw Exception("Unable to load texture from file: $file" )
    }

    fun loadGLTexture(file: String): Int {
        val tex = loadTexture(file)
        val texID = glGenTextures()
        glBindTexture(GL_TEXTURE_2D,texID)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        val internalFormat = when(tex.channels){
            4 -> GL_RGBA
            3 -> GL_RGB
            else -> throw Exception("Unsupported number of channels in image. ${tex.channels} channels")
        }
        val pixelFormat = when(tex.channels){
            4 -> GL_RGBA
            3 -> GL_RGB
            else -> throw Exception("Unsupported number of channels in image. ${tex.channels} channels")
        }
        glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, tex.width, tex.height, 0, pixelFormat, GL_UNSIGNED_BYTE, tex.data)
        glGenerateMipmap(GL_TEXTURE_2D)
        return texID
    }
}