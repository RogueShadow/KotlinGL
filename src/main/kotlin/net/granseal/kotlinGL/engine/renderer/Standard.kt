package net.granseal.kotlinGL.engine.renderer

import net.granseal.kotlinGL.engine.*
import net.granseal.kotlinGL.engine.math.Matrix4f
import net.granseal.kotlinGL.engine.math.Vector3f
import net.granseal.kotlinGL.engine.shaders.Depth
import net.granseal.kotlinGL.engine.shaders.ShaderManager
import org.lwjgl.opengl.GL33.*
import kotlin.properties.Delegates

class Standard(var width: Int,var height: Int): Renderer {
    var clearColor = Vector3f(0.2f,0.3f,0.4f)
    val shadowMapResolution = 2048
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
        e.draw(shadowMap.shader)
    }
    private fun renderFinal(e: Entity){
        e.draw()
    }

    class ShadowMap(val mapWidth: Int, val mapHeight: Int,val currentWidth: Int, val currentHeight: Int){
        var fboID: Int by Delegates.notNull()
        var texID: Int by Delegates.notNull()

        val shader = Depth(Matrix4f())

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
            val near = 1f
            val far = 150f
            val lightProj = Matrix4f.orthographic(-20f,20f,-40f,20f,near,far)
            //val lightProj = Matrix4f.perspective(90f,1f,near,far)
            val cam = Camera()
            if (LightManager.sunLamp != null) {
                cam.position(LightManager.sunPos.x,25f,LightManager.sunPos.z)
                cam.lookAt(cam.pos + LightManager.sunLamp!!.direction)
            }else{

            }
            val lightView = cam.view
            val lsm = lightProj * lightView
            shader.lightSpaceMatrix = lsm
            glCullFace(GL_FRONT)
        }

        fun end(){
            glBindFramebuffer(GL_FRAMEBUFFER,0)
            glViewport(0,0,currentWidth,currentHeight)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
            //config shaders matrices
            glBindTexture(GL_TEXTURE_2D,texID)
            ShaderManager.setAllMat4("lightSpaceMatrix",shader.lightSpaceMatrix)
            ShaderManager.setAllInt("shadowMap",texID)
            glCullFace(GL_BACK)
        }
    }
}