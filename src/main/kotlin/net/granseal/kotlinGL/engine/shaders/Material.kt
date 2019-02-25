package net.granseal.kotlinGL.engine.shaders

import com.hackoeur.jglm.Mat
import net.granseal.kotlinGL.engine.math.Vector3f
import org.lwjgl.opengl.GL33
import java.io.File
import kotlin.properties.Delegates

object ShaderManager {
    private val shaders = mutableListOf<ShaderProgram>()

    fun addShader(vertSrc: String, fragSrc: String): Int {
        val shader = ShaderProgram(vertSrc,fragSrc)
        shaders += shader
        println("New Shader: ${shader.javaClass.canonicalName} :: ${shader.id}")
        return shader.id
    }

    fun cleanUp(){
        shaders.forEach{
            GL33.glDeleteProgram(it.id)
            println("Deleting Shader: ${it.id}")
        }
        shaders.clear()
    }
    fun getShader(id: Int) = shaders.first{it.id == id}
}

class DefaultMaterial(val diffuse: Vector3f = Vector3f(0.7f,0.5f,0.2f),
               val specular: Vector3f = Vector3f(.5f, .5f, .5f),
               val shininess: Float = 32f): Material() {
    init {
        shaderID = ShaderManager.addShader(File("main.vert").readText(),File("main.frag").readText())
    }
    override fun use(){
        val shader = getShader()
        shader.setVec3("material.diffuse",diffuse)
        shader.setVec3("material.specular",specular)
        shader.setFloat("material.shininess",shininess)
    }
}

class LightMaterial(val color:Vector3f = Vector3f(1f,1f,1f)): Material() {
    init {
        shaderID = ShaderManager.addShader(File("main.vert").readText(),File("light.frag").readText())
    }
    override fun use() {
        val shader = getShader()
        shader.setVec3("color",color)
    }
}

abstract class Material {
    abstract fun use()
    fun getShader() = ShaderManager.getShader(shaderID)
    var shaderID: Int = -1
}