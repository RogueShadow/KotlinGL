package net.granseal.kotlinGL.engine

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL33.*
import org.lwjgl.stb.STBImage.stbi_load
import java.awt.image.BufferedImage
import java.nio.ByteBuffer


object TextureManager{

    val texids = mutableListOf<Int>()

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
        println("Loaded Texture: ID: $texID File: $file ${tex.width}x${tex.height}")
        texids += texID
        return texID
    }

    fun cleanUp(){
        texids.forEach{
            glDeleteTextures(it)
            println("Deleting Texture: $it")
        }
    }

    fun genTexture(): Int {
        val texID = glGenTextures()
        texids += texID
        return texID
    }

    fun loadBufferedImage(image: BufferedImage): Int {
        val buffer = getBufferedImageData(image)
        val texID = glGenTextures()
        glBindTexture(GL_TEXTURE_2D,texID)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, image.width, image.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer.flip())
        //glGenerateMipmap(GL_TEXTURE_2D)
        texids += texID
        println("Loaded Texture: ID: $texID BufferedImage: $image ${image.width}x${image.height}")
        return texID
    }

    fun updateTexture(image: BufferedImage, texID: Int){
        val buffer = getBufferedImageData(image)
        glBindTexture(GL_TEXTURE_2D,texID)
        glTexSubImage2D(GL_TEXTURE_2D,0,0,0,image.width,image.height,GL_RGBA, GL_UNSIGNED_BYTE, buffer)
        //GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D)
    }

    fun getBufferedImageData(image: BufferedImage): ByteBuffer {
        val pixels = IntArray(image.width * image.height)
        image.getRGB(0,0,image.width,image.height,pixels,0,image.width)
        val buffer = BufferUtils.createByteBuffer(image.width * image.height * 4)
        for (y in 0 until image.width){
            for (x in 0 until image.height){
                val pixel = pixels[(y) * image.width + x]
                buffer.put(((pixel.shr(16)) and 0xFF).toByte())
                buffer.put(((pixel.shr(8) ) and 0xFF).toByte())
                buffer.put(( pixel and 0xFF).toByte())
                buffer.put(((pixel.shr(24)) and 0xFF).toByte())
            }
        }

        return buffer.flip()
    }
}
