package net.granseal.kotlinGL.engine.renderer

import com.curiouscreature.kotlin.math.*
import net.granseal.kotlinGL.engine.*
import net.granseal.kotlinGL.engine.shaders.DefaultShader
import net.granseal.kotlinGL.engine.shaders.Depth
import net.granseal.kotlinGL.engine.shaders.ShaderManager
import org.lwjgl.opengl.GL33.*
import kotlin.properties.Delegates

class Standard(var width: Int,var height: Int): Renderer {
    var clearColor = Float3(0.2f,0.3f,0.4f)
    val shadowMapResolution = 2048 * 2
    lateinit var shadowMap: ShadowMap
    var renderPass = 0

    override fun initialize(){
        glEnable(GL_BLEND)
        glEnable(GL_MULTISAMPLE)
        glEnable(GL_DEPTH_TEST)
        glEnable(GL_CULL_FACE)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glClearColor(clearColor.x, clearColor.y, clearColor.z, 1.0f)
        shadowMap = ShadowMap(shadowMapResolution,shadowMapResolution,width,height)
        shadowMap.generate()
    }
    override fun render(e: Entity){
        when (renderPass){
            1 -> renderDepthPass(e)
            2 -> renderFinal(e)
            else -> throw Exception("Invalid render pass.")
        }
    }
    override fun next(): Boolean {
        return when (renderPass){
            0 -> {renderPass = 1;shadowMap.start();true}
            1 -> {renderPass = 2;shadowMap.end();true}
            2 -> {renderPass = 0;false }
            else -> throw Exception("Invalid render state.")
        }
    }
    private fun renderDepthPass(e: Entity){
        val shader = e.components().singleOrNull{it is DefaultShader}
        if (shader != null && !(shader as DefaultShader).castShadows)return
        e.draw(shadowMap.shader)
    }
    private fun renderFinal(e: Entity){
        e.draw(null)
    }

    class ShadowMap(val mapWidth: Int, val mapHeight: Int,val currentWidth: Int, val currentHeight: Int){
        var fboID: Int by Delegates.notNull()
        var texID: Int by Delegates.notNull()

        val shader = Depth()

        fun generate() {
            fboID = BufferManager.genFrameBuffer()
            texID = TextureManager.genTexture()
            glBindTexture(GL_TEXTURE_2D, texID)
            glTexImage2D(
                GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, mapWidth, mapHeight,
                0, GL_DEPTH_COMPONENT, GL_FLOAT, 0L
            )

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER)
            val borderColor = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)
            glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, borderColor)
            glBindFramebuffer(GL_FRAMEBUFFER,fboID)
            glFramebufferTexture2D(GL_FRAMEBUFFER,GL_DEPTH_ATTACHMENT,GL_TEXTURE_2D, texID, 0)
            glDrawBuffer(GL_NONE)
            glReadBuffer(GL_NONE)
            glBindFramebuffer(GL_FRAMEBUFFER, 0)
        }

        fun start(){
            glViewport(0,0,mapWidth,mapHeight)
            glBindFramebuffer(GL_FRAMEBUFFER,fboID)
            glClear(GL_DEPTH_BUFFER_BIT)

            if (LightManager.sunLamp != null) {
                val proj = ortho(-30f, 30f, -30f, 30f, 1f, 50f)
                val front = LightManager.sunLamp?.direction
                val pos = Float3(LightManager.sunPos.x, 30f, LightManager.sunPos.z)
                val lsm = proj * inverse(lookAt(pos, pos - front!!, Float3(0f, 1f, 0f)))

                ShaderManager.setGlobalUniform("lightSpaceMatrix", lsm)
            }
            glCullFace(GL_FRONT)
        }

        fun end(){
            glBindFramebuffer(GL_FRAMEBUFFER,0)
            glViewport(0,0,currentWidth,currentHeight)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
            glBindTexture(GL_TEXTURE_2D,texID)
            ShaderManager.setGlobalUniform("shadowMap",texID)
            glCullFace(GL_BACK)
        }
    }
}