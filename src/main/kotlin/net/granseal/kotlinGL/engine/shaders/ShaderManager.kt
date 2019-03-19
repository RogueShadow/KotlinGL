package net.granseal.kotlinGL.engine.shaders

import com.curiouscreature.kotlin.math.*
import net.granseal.kotlinGL.engine.ComponentImpl
import net.granseal.kotlinGL.engine.Config
import net.granseal.kotlinGL.engine.LightManager
import org.lwjgl.opengl.GL33.*
import java.io.File

object ShaderManager {
    private val shaders = mutableListOf<ShaderInfo>()

    fun addShader(vertSrc: String, fragSrc: String): Int {
        val shader = ShaderProgram(vertSrc,fragSrc)
        shaders += ShaderInfo(shader,mutableListOf())
        println("New Shader: $shader :: ${shader.id}")
        return shader.id
    }

    fun cleanUp(){
        shaders.forEach{
            glDeleteProgram(it.shader.id)
            println("Deleting Shader: ${it.shader.id}")
        }
        shaders.clear()
    }
    fun getShader(id: Int) = shaders.first{it.shader.id == id}.shader
    fun listenForUniforms(uniforms: List<String>,id: Int){
        shaders.single { it.shader.id == id }.uniforms.apply {
            clear()
            addAll(uniforms)
        }
    }

    fun setGlobalUniform(name: String, value: Mat4){
        shaders.filter{it.uniforms.contains(name)}.forEach{it.shader.setMat4(name,value)}
    }
    fun setGlobalUniform(name: String, value: Float3){
        shaders.filter{it.uniforms.contains(name)}.forEach{it.shader.setVec3(name,value)}
    }
    fun setGlobalUniform(name: String, value: Float){
        shaders.filter{it.uniforms.contains(name)}.forEach{it.shader.setFloat(name,value)}
    }
    fun setGlobalUniform(name: String, value: Int){
        shaders.filter{it.uniforms.contains(name)}.forEach{it.shader.setInt(name,value)}
    }

    data class ShaderInfo(val shader: ShaderProgram, val uniforms: MutableList<String>)
}

class DefaultShader(var diffuse: Float3 = Float3(0.7f,0.5f,0.2f),
                    var specular: Float3 = Float3(.5f, .5f, .5f),
                    var shininess: Float = 32f,
                    var tint: Float3 = Float3(0f,0f,0f),
                    var diffTexID: Int = -1,
                    var specTexID: Int = -1): Shader, ComponentImpl() {


    override fun use(transform: Mat4) {
        val shader = ShaderManager.getShader(shaderID)
        shader.setVec3("material.diffuse",diffuse)
        shader.setVec3("material.specular",specular)
        shader.setFloat("material.shininess",shininess)
        shader.setVec3("material.tint",tint)
        if (diffTexID != -1){
            glActiveTexture(GL_TEXTURE0)
            glBindTexture(GL_TEXTURE_2D,diffTexID)
            shader.setInt("material.diff_tex",0)
        }
        if (specTexID != -1){
            glActiveTexture(GL_TEXTURE1)
            glBindTexture(GL_TEXTURE_2D,specTexID)
            shader.setInt("material.spec_tex",1)
        }
        shader.setMat4("transform",transform)
        shader.setInt("material.use_diff_tex",if (diffTexID == -1)0 else 1)
        shader.setInt("material.use_spec_tex",if (specTexID == -1)0 else 1)
    }
    fun copy(diffuse: Float3 = this.diffuse,
             specular: Float3 = this.specular,
             shininess: Float = this.shininess,
             tint: Float3 = this.tint,
             diffTexID: Int = this.diffTexID,
             specTexID: Int = this.specTexID) = DefaultShader(diffuse,specular,shininess,tint,diffTexID,specTexID)

    companion object {
        val shaderID = ShaderManager.addShader(File(Config.SHADER_DIR + "main.vert").readText(),File(Config.SHADER_DIR + "main.frag").readText())
        init{
            val uniforms = mutableListOf<String>()
            (0..20).forEach{
                uniforms += "light[$it].position"
                uniforms += "light[$it].ambient"
                uniforms += "light[$it].diffuse"
                uniforms += "light[$it].specular"
                uniforms += "light[$it].constant"
                uniforms += "light[$it].linear"
                uniforms += "light[$it].quadratic"
            }
            uniforms += "sunlamp.direction"
            uniforms += "sunlamp.ambient"
            uniforms += "sunlamp.specular"
            uniforms += "sunlamp.diffuse"
            uniforms += "projection"
            uniforms += "view"
            uniforms += "viewPos"
            uniforms += "engine_number_of_lights"
            uniforms += "shadowMap"
            uniforms += "lightSpaceMatrix"
            uniforms += "elapsedTime"
            ShaderManager.listenForUniforms(uniforms,shaderID)
        }
    }
}

class SolidColor(var color:Float3 = Float3(1f,1f,1f)): Shader, ComponentImpl() {

    override fun use(transform: Mat4) {
        val shader = ShaderManager.getShader(shaderID)
        shader.setVec3("color",color)
        shader.setMat4("transform",transform)
    }
    companion object {
        val shaderID = ShaderManager.addShader(File(Config.SHADER_DIR + "main.vert").readText(),File(Config.SHADER_DIR + "light.frag").readText())
        init {
            val uniforms = mutableListOf<String>()
            uniforms += "projection"
            uniforms += "view"
            ShaderManager.listenForUniforms(uniforms, shaderID)
        }
    }
}

class Sprite(var texID: Int): Shader, ComponentImpl() {
    override fun use(transform: Mat4) {
        val shader = ShaderManager.getShader(shaderID)
        shader.setMat4("transform", transform)
        glBindTexture(GL_TEXTURE_2D,texID)
        shader.setInt("sprite",texID)
    }
    companion object {
        val shaderID = ShaderManager.addShader(File(Config.SHADER_DIR + "main.vert").readText(),File(Config.SHADER_DIR + "sprite.frag").readText())
        init {
            val uniforms = mutableListOf<String>()
            uniforms += "projection"
            uniforms += "view"
            ShaderManager.listenForUniforms(uniforms, shaderID)
        }
    }
}

class Depth: Shader, ComponentImpl() {
    override fun use(transform: Mat4) {
        val shader = ShaderManager.getShader(shaderID)
        shader.setMat4("transform", transform)
    }
    companion object {
        val shaderID = ShaderManager.addShader(File(Config.SHADER_DIR + "depth.vert").readText(),File(Config.SHADER_DIR + "depth.frag").readText())
        init{
            ShaderManager.listenForUniforms(listOf("lightSpaceMatrix","lightView","lightProj"),shaderID)
        }
    }
}

interface Shader {
    fun use(transform: Mat4)
}
class PointLight : Light, ComponentImpl() {
    override fun position(): Float3 {
        return parent.position
    }

    var ambient: Float3 = Float3(0.1f,0.1f,0.1f)
    var diffuse: Float3 = Float3(1f,1f,1f)
    var specular: Float3 = Float3(1f,1f,1f)
    var constant: Float = 1f
    var linear: Float = 0.09f
    var quadratic: Float = 0.032f

    init {
        LightManager.addLight(this)
    }
    override fun update(index: Int){
        with(ShaderManager){
            setGlobalUniform("light[$index].position" ,position())
            setGlobalUniform("light[$index].ambient"  ,ambient)
            setGlobalUniform("light[$index].diffuse"  ,diffuse)
            setGlobalUniform("light[$index].specular" ,specular)
            setGlobalUniform("light[$index].constant" ,constant)
            setGlobalUniform("light[$index].linear"   ,linear)
            setGlobalUniform("light[$index].quadratic",quadratic)
        }
    }
}
class SunLamp {
    var direction: Float3 = Float3(0.5f,-1f,0.5f)
        set(value){
            field = value
            ShaderManager.setGlobalUniform("sunlamp.direction",value)
        }
    var ambient: Float3 = Float3(0.05f,0.05f,0.05f)
        set(value){
            field = value
            ShaderManager.setGlobalUniform("sunlamp.ambient",value)
        }
    var diffuse: Float3 = Float3(0.25f,0.25f,0.25f)
        set(value){
            field = value
            ShaderManager.setGlobalUniform("sunlamp.diffuse",value)
        }
    var specular: Float3 = Float3(0.25f,0.25f,0.25f)
        set(value){
            field = value
            ShaderManager.setGlobalUniform("sunlamp.specular",value)
        }
    init {
        with(ShaderManager){
            setGlobalUniform("sunlamp.direction",direction)
            setGlobalUniform("sunlamp.ambient",ambient)
            setGlobalUniform("sunlamp.diffuse",diffuse)
            setGlobalUniform("sunlamp.specular",specular)
        }
    }
}

interface Light{
    fun position(): Float3
    fun update(index: Int)
}