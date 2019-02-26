package net.granseal.kotlinGL.engine

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL33
import org.lwjgl.opengl.GL33.*
import org.lwjgl.stb.STBImage.stbi_load
import java.awt.image.BufferedImage
import java.nio.ByteBuffer



object TextureLoader{

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
            GL33.glDeleteTextures(it)
            println("Deleting Texture: $it")
        }
    }

    fun loadBufferedImage(image: BufferedImage): Int {
        val pixels = IntArray(image.width * image.height)
        image.getRGB(0,0,image.width,image.height,pixels,0,image.width)

        val buffer = BufferUtils.createByteBuffer(image.width * image.height * 4)

        for (x in 0 until image.width){
            for (y in 0 until image.height){
                val pixel = pixels[(y) * image.width + x]
                buffer.put(((pixel.shr(16)) and 0xFF).toByte())
                buffer.put(((pixel.shr(8) ) and 0xFF).toByte())
                buffer.put(( pixel and 0xFF).toByte())
                buffer.put(((pixel.shr(24)) and 0xFF).toByte())
            }
        }
        val texID = GL33.glGenTextures()
        glBindTexture(GL_TEXTURE_2D,texID)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_POINT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_POINT)
        glTexImage2D(GL_TEXTURE_2D, 0, GL33.GL_RGBA, image.width, image.height, 0, GL33.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer.flip())
        glGenerateMipmap(GL_TEXTURE_2D)
        texids += texID
        println("Loaded Texture: ID: $texID BufferedImage: $image ${image.width}x${image.height}")
        return texID
    }
}
//    private static void renderImage(BufferedImage image){
//        int[] pixels = new int[image.getWidth() * image.getHeight()];
//        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
//
//        ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 3); //4 for RGBA, 3 for RGB
//
//        for(int y = 0; y < image.getHeight(); y++){
//            for(int x = 0; x < image.getWidth(); x++){
//            int pixel = pixels[y * image.getWidth() + x];
//            buffer.put((byte) ((pixel >> 16) & 0xFF));     // Red component
//            buffer.put((byte) ((pixel >> 8) & 0xFF));      // Green component
//            buffer.put((byte) (pixel & 0xFF));               // Blue component
//            buffer.put((byte) ((pixel >> 24) & 0xFF));    // Alpha component. Only for RGBA
//        }
//        }
//
//        buffer.flip(); //FOR THE LOVE OF GOD DO NOT FORGET THIS

