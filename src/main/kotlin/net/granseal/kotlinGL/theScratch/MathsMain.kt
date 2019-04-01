package net.granseal.kotlinGL.theScratch

import com.curiouscreature.kotlin.math.*
import net.granseal.kotlinGL.engine.*
import net.granseal.kotlinGL.engine.renderer.Renderer
import net.granseal.kotlinGL.engine.shaders.DefaultShader
import net.granseal.kotlinGL.engine.shaders.PointLight
import net.granseal.kotlinGL.engine.shaders.SolidColor
import net.granseal.kotlinGL.engine.shaders.SunLamp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

fun main(args: Array<String>) {
    MathsMain(800, 600, "KotlinGL", false).run()
}

class MathsMain(width: Int, height: Int, title: String,fullScreen: Boolean) : KotlinGL(width, height, title,fullScreen) {
    lateinit var cube: Entity
    var rand = Random(System.nanoTime())
    val pixels = mutableListOf<Entity>()
    fun rColor() = Float3(rand.nextFloat(),rand.nextFloat(),rand.nextFloat())

    override fun mouseDown(button: Int, action: Int, mousex: Float, mousey: Float) {

    }

    override fun keyEvent(key: String, action: Int) {
    }

    override fun initialize() {
        mouseGrabbed = false
        camera.projection = ortho(0f, width.toFloat()/2, height.toFloat()/2,0f,-20f,20f)
        val cubeMesh = MeshManager.loadObj("flatcube.obj")

        pixels += Entity().apply {
            position = Float3(width/4f,height/4f,0f)
            scale = Float3(10f, 10f, 10f)
            addComponent(cubeMesh)
            addComponent(SolidColor(rColor()))
        }

        val pos = Float3(0f,1f,-20f)
        var look = lookAt(pos,pos+Float3(0f,0f,1f),Float3(0f,1f,0f))
        var testPos = Float4(1f,1f,1f,1f)
        var resultPos = camera.projection * look * testPos
        println("Projection\n${camera.projection}\nLookAt\n$look\nTestPoint\n$testPos\nResult\n$resultPos")

        println("projView\n${camera.projection * look}")

    }

    override fun update(delta: Float, deltax: Float, deltay: Float) {
        setTitle("MathTest fps: ${getFPS()}")
        pixels.forEach { it.update(delta) }
        LightManager.calculateLightIndex(camera.pos)
    }

    override fun draw(renderer: Renderer) {
        pixels.forEach { renderer.render(it) }
    }

    override fun mouseMoved(mouseX: Float, mouseY: Float, deltaX: Float, deltaY: Float) {
    }

    override fun mouseScrolled(delta: Float) {
    }

}

object Singleton