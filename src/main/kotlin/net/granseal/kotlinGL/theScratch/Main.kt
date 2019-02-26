package net.granseal.kotlinGL.theScratch

import net.granseal.kotlinGL.engine.Entity
import net.granseal.kotlinGL.engine.KotlinGL
import net.granseal.kotlinGL.engine.MeshManager
import net.granseal.kotlinGL.engine.math.Vector3f
import net.granseal.kotlinGL.engine.shaders.*
import org.lwjgl.glfw.GLFW.*
import kotlin.math.cos
import kotlin.math.sin

fun main() {
    Main(1600, 900, "KotlinGL", false).run()
}

class Main(width: Int, height: Int, title: String,fullScreen: Boolean) : KotlinGL(width, height, title,fullScreen) {
    override fun mouseClicked(button: Int,action: Int, mousex: Float, mousey: Float) {
        if (button == 0 && action == 0){
            mouseGrabbed = !mouseGrabbed
        }
    }

    var entities: MutableList<Entity> = mutableListOf()
    var selected = 0
    var moveBoxes = false

    lateinit var light: LightConfig

    private lateinit var lightEntity: Entity
    lateinit var floor: Entity

    override fun initialize() {

        camera.setPerspective(45f, width.toFloat() / height.toFloat(), 0.1f, 100f)

        lightEntity = Entity(MeshManager.loadObj("flatcube.obj"),LightShader())
        floor = Entity(MeshManager.loadObj("ground.obj"),DefaultShader(diffuse = Vector3f(0.2f,0.7f,0.1f)))
        floor.position(0f, -5f, 0f)
        lightEntity.position(1.2f, 1f, 2f)
        light = LightConfig( )


        lightEntity.scale = 0.1f
        entities.add(lightEntity)
        entities.add(floor)

        entities.add(Entity(MeshManager.loadObj("dragon.obj"),DefaultShader()).apply { scale = 0.1f })
        entities.add(Entity(MeshManager.loadObj("flatcube.obj"),DefaultShader()).apply { position(2f,1f,2f) })
        entities.add(Entity(MeshManager.loadObj("cube.obj"),DefaultShader(diffuse = Vector3f(0.3f,0.4f,0.8f))).apply { position(-2f,1.5f,0f) })


    }

    override fun mouseMoved(mouseX: Float, mouseY: Float, deltaX: Float, deltaY: Float) {}
    override fun mouseScrolled(delta: Float) {}
    //key is case sensative, only normal typing keys, action 1 - pressed, 2 - held (repeats), 0 - released
    override fun keyEvent(key: String, action: Int) {
        if (action == 1) {
            if (!moveBoxes) return
            when (key) {
                "q" -> {
                    selected++
                    if (selected >= entities.size) selected = 0
                }
                "e" -> {
                    selected--
                    if (selected < 0) selected = entities.size - 1
                }

                "a" -> entities[selected].move(-1f, 0f, 0f)
                "d" -> entities[selected].move(1f, 0f, 0f)
                "w" -> entities[selected].move(0f, 1f, 0f)
                "s" -> entities[selected].move(0f, -1f, 0f)
                "z" -> entities[selected].move(0f, 0f, 1f)
                "c" -> entities[selected].move(0f, 0f, -1f)
            }
        }
    }

    override fun update(delta: Float, deltax: Float, deltay: Float) {
        setTitle("$windowTitle ${getFPS()}")

        //entities[Random.nextInt(entities.size-1)].rotate(100f*delta,0.5f,1f,0f)
        entities.forEach {
            if (it != floor) it.rotate(150f * delta, 0.0f, 1f, 0f)
        }
        //entities[selected].position((mouseX/width)*8 - 1,(1-(mouseY/height))*8 - 1,-5f)
        //entities[selected].scale(sin(getTimePassed()).toFloat(),sin(getTimePassed()).toFloat(),sin(getTimePassed().toFloat()))

        val radius = 5f
        //cam.pos.x = sin(getTimePassed()*0.5f).toFloat() * radius
        //cam.pos.z = cos(getTimePassed()*0.5f).toFloat() * radius
        lightEntity.position.x = sin(getTimePassed() * 0.5f).toFloat() * radius
        lightEntity.position.z = cos(getTimePassed() * 0.5f).toFloat() * radius


        if (keyPressed(GLFW_KEY_LEFT_SHIFT)) {
            moveBoxes = true
        } else {
            moveBoxes = false

            if (keyPressed(GLFW_KEY_W)) camera.forward( delta)
            if (keyPressed(GLFW_KEY_S)) camera.backword( delta)
            if (keyPressed(GLFW_KEY_A)) camera.right( delta)
            if (keyPressed(GLFW_KEY_D)) camera.left( delta)
            if (keyPressed(GLFW_KEY_SPACE)) camera.up( delta)
            if (keyPressed(GLFW_KEY_LEFT_CONTROL)) camera.down( delta)
            if (keyPressed(GLFW_KEY_X)) camera.lookAt(Vector3f())
        }

        light.position = lightEntity.position
    }

    override fun draw() {
        entities.withIndex().forEach {
            if (it.index == selected) {
                if (it.value.material is DefaultShader)it.value.material
            } else {
                if (it.value.material is DefaultShader) (it.value.material as DefaultShader).tint = Vector3f(0f,0f,0f)
            }
            it.value.draw()
        }
    }
}

