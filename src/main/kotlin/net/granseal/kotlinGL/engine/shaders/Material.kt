package net.granseal.kotlinGL.engine.shaders

import net.granseal.kotlinGL.engine.math.Matrix4f
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
    fun setAllVec3(name: String, value: Vector3f){
        shaders.forEach {it.setVec3(name,value)}
    }
    fun setAllMat4(name: String, value: Matrix4f){
        shaders.forEach {it.setMat4(name,value)}
    }
    fun setAllLight(light: LightConfig){
        shaders.forEach{
            it.setVec3("light.position",light.position)
            it.setVec3("light.diffuse",light.diffuse)
            it.setVec3("light.ambient",light.ambient)
            it.setVec3("light.specular",light.specular)
        }
    }
}

class DefaultShader(var diffuse: Vector3f = Vector3f(0.7f,0.5f,0.2f),
                    var specular: Vector3f = Vector3f(.5f, .5f, .5f),
                    var shininess: Float = 32f,
                    var tint: Vector3f = Vector3f(0f,0f,0f),
                    var diffTexID: Int = -1,
                    var specTexID: Int = -1): Material() {

    override fun use(transform: Matrix4f) {
        val shader = ShaderManager.getShader(shaderID)
        shader.setVec3("material.diffuse",diffuse)
        shader.setVec3("material.specular",specular)
        shader.setFloat("material.shininess",shininess)
        shader.setVec3("material.tint",tint)
        if (diffTexID != -1){
            GL33.glActiveTexture(GL33.GL_TEXTURE0)
            GL33.glBindTexture(GL33.GL_TEXTURE_2D,diffTexID)
            shader.setInt("material.diff_tex",0)
        }
        if (specTexID != -1){
            GL33.glActiveTexture(GL33.GL_TEXTURE1)
            GL33.glBindTexture(GL33.GL_TEXTURE_2D,specTexID)
            shader.setInt("material.spec_tex",1)
        }
        shader.setMat4("transform",transform)
        shader.setInt("material.use_diff_tex",if (diffTexID == -1)0 else 1)
        shader.setInt("material.use_spec_tex",if (specTexID == -1)0 else 1)
    }
    fun copy(diffuse: Vector3f = this.diffuse,
             specular: Vector3f = this.specular,
             shininess: Float = this.shininess,
             tint: Vector3f = this.tint) = DefaultShader(diffuse,specular,shininess,tint)

    companion object {
        val shaderID = ShaderManager.addShader(File("main.vert").readText(),File("main.frag").readText())
    }
}

class LightShader(var color:Vector3f = Vector3f(1f,1f,1f)): Material() {
    override fun use(transform: Matrix4f) {
        val shader = ShaderManager.getShader(shaderID)
        shader.setVec3("color",color)
        shader.setMat4("transform",transform)
    }
    companion object {
        val shaderID = ShaderManager.addShader(File("main.vert").readText(),File("light.frag").readText())
    }
}

//interface Material {
//    fun use(transform: Matrix4f)
//}
abstract class Material{
    abstract fun use(transform: Matrix4f)
}

class LightConfig {
    var position: Vector3f by Delegates.observable(Vector3f(1f,1f,1f)){property, old, new ->
        ShaderManager.setAllVec3("light.position",new)
    }
    var ambient: Vector3f = Vector3f(0.1f,0.1f,0.1f)
    var diffuse: Vector3f = Vector3f(1f,1f,1f)
    var specular: Vector3f = Vector3f(1f,1f,1f)
    init {
        ShaderManager.setAllLight(this)
    }
}
