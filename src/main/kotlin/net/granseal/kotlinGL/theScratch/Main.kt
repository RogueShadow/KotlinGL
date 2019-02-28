package net.granseal.kotlinGL.theScratch

import net.granseal.kotlinGL.engine.Entity
import net.granseal.kotlinGL.engine.KotlinGL
import net.granseal.kotlinGL.engine.MeshManager
import net.granseal.kotlinGL.engine.TextureLoader
import net.granseal.kotlinGL.engine.math.Matrix3f
import net.granseal.kotlinGL.engine.math.Vector3f
import net.granseal.kotlinGL.engine.shaders.*
import org.lwjgl.glfw.GLFW.*
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

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
    lateinit var lights: MutableList<PointLight>

    val rand = Random(System.nanoTime())

    lateinit var light: PointLight

    private lateinit var lightEntity: Entity
    lateinit var floor: Entity

    override fun initialize() {

        val bi = BufferedImage(64,64,BufferedImage.TYPE_INT_ARGB)
        val g2d = bi.createGraphics()
        g2d.color = Color.DARK_GRAY
        g2d.fillRect(0,0,64,64)
        g2d.color = Color.green
        g2d.drawString("Hello World",0,30)


        //ImageIO.write(bi,"png", File("test.png"))


        camera.setPerspective(45f, width.toFloat() / height.toFloat(), 0.1f, 100f)

        lightEntity = Entity(MeshManager.loadObj("flatcube.obj"),SolidColor())

        floor = Entity(MeshManager.loadObj("ground.obj"),DefaultShader(diffuse = Vector3f(1f,1f,1f)))
        floor.position(0f, -5f, 0f)
        lightEntity.position(1.2f, 1f, 2f)
        light = PointLight( ).apply { linear = 0.4f }
        lights = mutableListOf<PointLight>()
        (1..10).forEach{
            val p = PointLight()
            p.position.x = -20f + it*5 + rand.nextFloat()
            p.position.z = rand.nextDouble(-20.0,20.0).toFloat()
            p.position.y = rand.nextDouble(1.0,5.0).toFloat()
            p.diffuse.x = rand.nextFloat()
            p.diffuse.y = rand.nextFloat()
            p.diffuse.z = rand.nextFloat()
            p.diffuse.normalize()
            p.ambient = p.diffuse
            p.specular = p.diffuse
            p.linear = 0.5f
            lights.add(p)
        }
        lights.forEach{
            entities.add( Entity(MeshManager.loadObj("flatcube.obj"),SolidColor(Vector3f(it.diffuse.x,it.diffuse.y,it.diffuse.z))).apply {
                position = it.position
                scale = 0.1f
            })
        }

        LightManager.calculateLightIndex(camera.pos)

        lightEntity.scale = 0.1f
        entities.add(lightEntity)
        entities.add(floor)

        entities.add(Entity(MeshManager.loadObj("dragon.obj"),DefaultShader()).apply { scale = 0.1f })
        entities.add(Entity(MeshManager.loadObj("flatcube.obj"),DefaultShader(diffTexID = TextureLoader.loadBufferedImage(bi))).apply { position(2f,1f,2f) })
        entities.add(Entity(MeshManager.loadObj("flatcube.obj"),DefaultShader(diffTexID = TextureLoader.loadGLTexture("container2.png"),specTexID = TextureLoader.loadGLTexture("container2_specular.png"))).apply { position(-2f,1.5f,0f) })


        //SunLamp()
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

        val angle = 1f * delta
        val rotate = Matrix3f(Vector3f(cos(angle),0f,sin(angle)),
                              Vector3f(0f,1f,0f),
            Vector3f(-sin(angle),0f,cos(angle)))



        lights.forEach{
            it.position = rotate.multiply(it.position)
        }

        //entities[Random.nextInt(entities.size-1)].rotate(100f*delta,0.5f,1f,0f)
        entities.forEach {
            if (it != floor) it.rotate(10f * delta, 0.0f, 1f, 0f)
            if (it.material is SolidColor)it.position = rotate.multiply(it.position)
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
        LightManager.calculateLightIndex(camera.pos)
    }

    override fun draw() {
        lateinit var mat: DefaultShader
        entities.withIndex().forEach {
            if (it.value.material is DefaultShader){
                mat = it.value.material as DefaultShader
                if (it.index == selected) {
                    mat.tint = Vector3f(0.1f, 0.3f, 0.4f)
                } else {
                    mat.tint = Vector3f(0f, 0f, 0f)
                }
            }
            it.value.draw()
        }
    }
}

object LightManager{
    const val MAX_LIGHTS = 16

    fun addLight(pointLight: Light) {
        lights += pointLight
    }

    fun calculateLightIndex(pos: Vector3f){
        lights.sortBy{(it.position - pos).length()}
        lights.withIndex()
              .take(MAX_LIGHTS)
              .forEach{
              it.value.update(it.index)
        }
        ShaderManager.setAllInt("engine_number_of_lights",if (lights.size <= MAX_LIGHTS) lights.size else MAX_LIGHTS)
    }

    val lights = mutableListOf<Light>()
}