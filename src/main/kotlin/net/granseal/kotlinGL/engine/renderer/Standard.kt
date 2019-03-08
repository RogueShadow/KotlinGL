package net.granseal.kotlinGL.engine.renderer

import net.granseal.kotlinGL.engine.*
import net.granseal.kotlinGL.engine.math.Matrix4f
import net.granseal.kotlinGL.engine.math.Vector3f
import net.granseal.kotlinGL.engine.shaders.Depth
import net.granseal.kotlinGL.engine.shaders.Light
import net.granseal.kotlinGL.engine.shaders.ShaderManager
import org.lwjgl.opengl.GL33
import kotlin.properties.Delegates

class Standard(var width: Int,var height: Int): Renderer {
    val shadowMapResolution = 2048
    lateinit var shadowMap: ShadowMap
    var renderPass = 0

    override fun initialize(){
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
            GL33.glBindTexture(GL33.GL_TEXTURE_2D, texID)
            GL33.glTexImage2D(
                GL33.GL_TEXTURE_2D, 0, GL33.GL_DEPTH_COMPONENT, mapWidth, mapHeight,
                0, GL33.GL_DEPTH_COMPONENT, GL33.GL_FLOAT, 0L
            )

            GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR)
            GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR)
            GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_BORDER)
            GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_BORDER)
            val borderColor = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)
            GL33.glTexParameterfv(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_BORDER_COLOR, borderColor)
            GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER,fboID)
            GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER,GL33.GL_DEPTH_ATTACHMENT,GL33.GL_TEXTURE_2D, texID, 0)
            GL33.glDrawBuffer(GL33.GL_NONE)
            GL33.glReadBuffer(GL33.GL_NONE)
            GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0)
        }

        fun start(){
            GL33.glViewport(0,0,mapWidth,mapHeight)
            GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER,fboID)
            GL33.glClear(GL33.GL_DEPTH_BUFFER_BIT)
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
            GL33.glCullFace(GL33.GL_FRONT)
        }

        fun end(){
            GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER,0)
            GL33.glViewport(0,0,currentWidth,currentHeight)
            GL33.glClear(GL33.GL_COLOR_BUFFER_BIT or GL33.GL_DEPTH_BUFFER_BIT)
            //config shaders matrices
            GL33.glBindTexture(GL33.GL_TEXTURE_2D,texID)
            ShaderManager.setAllMat4("lightSpaceMatrix",shader.lightSpaceMatrix)
            ShaderManager.setAllInt("shadowMap",texID)
            GL33.glCullFace(GL33.GL_BACK)
        }
    }
}