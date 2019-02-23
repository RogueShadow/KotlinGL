package net.granseal.kotlinGL.theScratch

import net.granseal.kotlinGL.engine.*
import net.granseal.kotlinGL.engine.math.Vector3f
import net.granseal.kotlinGL.engine.shaders.Light
import net.granseal.kotlinGL.engine.shaders.Material
import net.granseal.kotlinGL.engine.shaders.ShaderProgram
import org.lwjgl.glfw.GLFW.*
import java.io.File
import kotlin.math.cos
import kotlin.math.sin

fun main() {
    Main(1920, 1080, "KotlinGL", true).run()
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

    lateinit var program: ShaderProgram
    lateinit var light: ShaderProgram

    var cam = Camera()
    var camSpeed = 5f

    private lateinit var lightEntity: Entity
    lateinit var floor: Entity

    override fun initialize() {


        val vertexShaderSource = File("main.vert").readText()
        val fragmentShaderSource = File("main.frag").readText()
        val lightShaderSource = File("light.frag").readText()

        light = ShaderProgram(vertexShaderSource, lightShaderSource)
        program = ShaderProgram(vertexShaderSource, fragmentShaderSource)

        light.setUniform3f("lightColor", 1f, 1f, 1f)
        program.setUniform3f("lightColor", 1f, 1f, 1f)

        cam.setPerspective(45f, width.toFloat() / height.toFloat(), 0.1f, 100f)

        program.setUniformMat4("projection", cam.projection)
        light.setUniformMat4("projection", cam.projection)

        program.setMaterial(Material.getDefaultMaterial())

        lightEntity = Entity(
            VertexArrayObject(
                Model.loadObj("flatcube.obj").apply { }, light
            )
        )
        floor = Entity(
            VertexArrayObject(
                Model.loadObj("ground.obj").apply {
                    textureFile = "GroundForest003_COL_VAR1_3K.jpg"
                    objectColor = Vector3f(0.2f, 0.7f, 0f)
                }, program
            )
        )
        floor.position(0f, -5f, 0f)

        lightEntity.position(1.2f, 1f, 2f)
        program.setUniform3f("light.position", 1.2f, 1f, 2f)
        program.setLight(Light.getDefaultLight())
        program.setVec3("viewPos", cam.pos)


        lightEntity.scale = 0.1f
        entities.add(lightEntity)
        entities.add(floor)

        entities.add(
            Entity(
                VertexArrayObject(
                    Model.loadObj(
                        "dragon.obj"
                    ).apply {
                        textureFile = "container.jpg"
                        objectColor = Vector3f(1f, 0.5f, 0.31f)
                    }, program
                )
            ).apply {
                position(2f, 0f, 2f)
                scale = 0.2f
            })
        entities.add(
            Entity(
                VertexArrayObject(
                    Model.loadObj(
                        "flatcube.obj"
                    ).apply {
                        textureFile = "awesomeface2.png"
                        objectColor = Vector3f(1f, 0.5f, 0.31f)
                    }, program
                )
            ).apply {
                position(-2f, 0f, 0f)
            })
        entities.add(
            Entity(
                VertexArrayObject(
                    Model.loadObj(
                        "cube.obj"
                    ).apply {
                        textureFile = "container.jpg"
                        objectColor = Vector3f(1f, 0.5f, 0.31f)
                    }, program
                )
            ).apply { position(0f, 0f, -2f) })


        //entities[0].rotate(90f,1f,0f,0f)


        cam.yaw = 2.0

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
        program.setVec3("light.position", lightEntity.position)

        if (keyPressed(GLFW_KEY_LEFT_SHIFT)) {
            moveBoxes = true
        } else {
            moveBoxes = false

            if (keyPressed(GLFW_KEY_W)) cam.forward(camSpeed * delta)
            if (keyPressed(GLFW_KEY_S)) cam.backword(camSpeed * delta)
            if (keyPressed(GLFW_KEY_A)) cam.right(camSpeed * delta)
            if (keyPressed(GLFW_KEY_D)) cam.left(camSpeed * delta)
            if (keyPressed(GLFW_KEY_SPACE)) cam.up(camSpeed * delta)
            if (keyPressed(GLFW_KEY_LEFT_CONTROL)) cam.down(camSpeed * delta)
            if (keyPressed(GLFW_KEY_X)) cam.lookAt(Vector3f())
        }


        cam.updateCamera(deltax, deltay, delta)
        //view = cam.lookAt(Vector3f(0f,0f,0f))
        program.setVec3("viewPos", cam.pos)
        program.setUniformMat4("view", cam.view)
        light.setUniformMat4("view", cam.view)

    }

    override fun draw() {
        entities.withIndex().forEach {
            if (it.index == selected) {
                it.value.vao.shader.setUniform3f("tint", 0f, 0.5f, 1f)
            } else {
                it.value.vao.shader.setUniform3f("tint", 0f, 0f, 0f)
            }
            it.value.draw()
        }


    }
}

