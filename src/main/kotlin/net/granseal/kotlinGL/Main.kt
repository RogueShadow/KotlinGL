package net.granseal.kotlinGL

import org.lwjgl.glfw.GLFW.*
import java.io.File
import kotlin.math.cos
import kotlin.math.sin

fun main(){
    Main(800,600,"KotlinGL").run()
}
class Main(width: Int, height: Int, title: String): KotlinGL(width,height,title) {
    var entities: MutableList<Entity> = mutableListOf()
    var selected = 0
    var moveBoxes = false

    lateinit var program: ShaderProgram
    lateinit var light: ShaderProgram

    var cam = Camera()
    var camSpeed = 5f

    override fun initialize(){

        val vertexShaderSource = File("main.vert").readText()
        val fragmentShaderSource = File("main.frag").readText()
        val lightShaderSource = File("light.frag").readText()

        light = ShaderProgram(vertexShaderSource,lightShaderSource)
        program = ShaderProgram(vertexShaderSource,fragmentShaderSource)

        light.setUniform3f("lightColor",1f,1f,1f)
        program.setUniform3f("lightColor",1f,1f,1f)

        cam.setPerspective(45f,width.toFloat()/height.toFloat(),0.1f,100f)

        program.setUniformMat4("projection",cam.projection)
        light.setUniformMat4("projection",cam.projection)

        val lightEntity = Entity(VertexArrayObject(Model.getCube().apply { objectColor = Vector3f(1f,0.5f,0.31f) },light))

        lightEntity.position(1.2f,1f,2f)
        program.setUniform3f("lightPos",1.2f,1f,2f)

        lightEntity.scale(0.2f,0.2f,0.2f)
        entities.add(lightEntity)
        entities.add(Entity(VertexArrayObject(Model.getCube().apply { },program)))
        entities.add(Entity(VertexArrayObject(Model.getCube().apply {  },program)))
        entities.add(Entity(VertexArrayObject(Model.getCube("container.jpg").apply {  },program)))
        entities.add(Entity(VertexArrayObject(Model.getCube("awesomeface2.png").apply {  },program)))



        //entities[0].rotate(90f,1f,0f,0f)
        //entities[0].scale(20f,1f,20f)

        cam.yaw = 2.0

    }

    override fun mouseMoved(mouseX: Float, mouseY: Float, deltaX: Float, deltaY: Float) {}
    override fun mouseScrolled(delta: Float) {}
    //key is case sensative, only normal typing keys, action 1 - pressed, 2 - held (repeats), 0 - released
    override fun keyEvent(key: String, action: Int) {
        if (action == 1) {
            if (!moveBoxes)return
            when (key){
                "q" -> {
                    selected++
                    if (selected >= entities.size) selected = 0
                }
                "e" -> {
                    selected--
                    if (selected < 0) selected = entities.size - 1
                }

                "a" ->entities[selected].move(-1f,0f,0f)
                "d" ->entities[selected].move(1f,0f,0f)
                "w" ->entities[selected].move(0f,1f,0f)
                "s" ->entities[selected].move(0f,-1f,0f)
                "z" ->entities[selected].move(0f,0f,1f)
                "c" ->entities[selected].move(0f,0f,-1f)
            }
        }
    }
    override fun update(delta: Float, deltax: Float, deltay: Float) {
        setTitle("$TITLE ${getFPS()}")

        //entities.forEach {it.rotate(100f*delta,0.5f,1f,0f)}
        //entities[selected].position((mouseX/width)*8 - 1,(1-(mouseY/height))*8 - 1,-5f)
        //entities[selected].scale(sin(getTimePassed()).toFloat(),sin(getTimePassed()).toFloat(),sin(getTimePassed().toFloat()))

        val radius = 10f
        //cam.pos.x = sin(getTimePassed()*0.5f).toFloat() * radius
        //cam.pos.z = cos(getTimePassed()*0.5f).toFloat() * radius


        if (keyPressed(GLFW_KEY_SPACE)){
            moveBoxes = true
        }else {
            moveBoxes = false

            if (keyPressed(GLFW_KEY_W)) cam.forward(camSpeed * delta)
            if (keyPressed(GLFW_KEY_S)) cam.backword(camSpeed * delta)
            if (keyPressed(GLFW_KEY_A)) cam.right(camSpeed * delta)
            if (keyPressed(GLFW_KEY_D)) cam.left(camSpeed * delta)
            if (keyPressed(GLFW_KEY_Z)) cam.up(camSpeed * delta)
            if (keyPressed(GLFW_KEY_C)) cam.down(camSpeed * delta)

            if (keyPressed(GLFW_KEY_X)) cam.lookAt(Vector3f())
        }


        cam.updateCamera(deltax,deltay,delta)
        //view = cam.lookAt(Vector3f(0f,0f,0f))
        program.setUniformMat4("view",cam.view)
        light.setUniformMat4("view",cam.view)

    }
    override fun draw() {
        entities.withIndex().forEach{
            if (it.index == selected){
                it.value.vao.shader.setUniform3f("tint",0f,0.5f,1f)
            }else{
                it.value.vao.shader.setUniform3f("tint",0f,0f,0f)
            }
            it.value.draw()
        }


    }
}

