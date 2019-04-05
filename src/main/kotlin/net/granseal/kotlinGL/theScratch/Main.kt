package net.granseal.kotlinGL.theScratch

import com.curiouscreature.kotlin.math.*
import net.granseal.kotlinGL.engine.*
import net.granseal.kotlinGL.engine.components.ComponentImpl
import net.granseal.kotlinGL.engine.components.Rotator
import net.granseal.kotlinGL.engine.renderer.Renderer
import net.granseal.kotlinGL.engine.shaders.*
import org.jetbrains.kotlin.backend.common.onlyIf
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL33.*
import javax.script.ScriptContext
import javax.script.ScriptEngineManager
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

fun main() {
    val main = Main(1600, 900, "KotlinGL", false)
    main.run()
}

class Main(width: Int, height: Int, title: String, fullScreen: Boolean) : KotlinGL(width, height, title, fullScreen) {
    val root = Entity()
    val draw = VectorDraw()

    fun loadSaveFile(g: Data.SaveFile): List<Entity> {
        val list = mutableListOf<Entity>()
        g.objects.forEach {
            val e = Entity()
            when (it.type) {
                "Box" -> e.addComponent(getMesh("box"))
            }
            e.position = it.position
            e.scale = Float3(it.scale)
            e.addComponent(SolidColor(it.color))
            list += e
        }
        return list
    }

    override fun initialize() {
        loadResourceFile("resources.kgl")
        draw.init()

        val g = Data.SaveFile()
        with(g.objects) {
            add(Data.KglObject("Box", color = rColor()))
            add(Data.KglObject("Sphere", color = rColor()))
        }

        camera.projection = perspective(45f, width.toFloat() / height.toFloat(), 0.1f, 200f)

        root.addChild {
            addComponent(Sprite(renderer.shadowMap.texID))
            addComponent(getMesh("quad"))
            addComponent(PointLight())
            position = Float3(0f, 3f, 0f)
        }

        root.addChild {
            addComponent(getMesh("dragon"))
            addComponent(DefaultShader(diffuse = Float3(0.5f, 0.8f, 0.3f)))
            position = Float3(0f, 8f, 0f)
            scale = Float3(.5f)
            addComponent(Rotator(45f,Float3(0.5f, 0.5f, 0.5f)))
        }

        root.addChild {
            addComponent(
                DefaultShader(
                    diffTexID = getTex("ground"),
                    shininess = 0.1f
                )
            )
            addComponent(getMesh("terrain"))
            position = Float3(-40f, -100f, -40f)
        }


        repeat(17) {
            val e = Entity()
            val p = PointLight()
            e.position.x = -20f + it * 5 + rand.nextFloat()
            e.position.z = rand.nextDouble(-80.0, 80.0).toFloat()
            e.position.y = rand.nextDouble(3.0, 15.0).toFloat()
            p.diffuse = rColor()
            p.ambient = p.diffuse * 0.1f
            p.specular = p.diffuse
            p.linear = 0.25f
            e.addComponent(SolidColor(p.diffuse))
            e.addComponent(getMesh("box"))
            e.addComponent(p)
            e.id = "Light"
            e.scale = Float3(0.2f)
            root.addChild(e)
            e.addComponent {
                object : ComponentImpl() {
                    val offsetS = rand.nextFloat()
                    val offset = rand.nextFloat() * 0.0025f
                    val rot = Mat3(
                        Float3(cos(offset), 0f, sin(offset)),
                        Float3(0f, 1f, 0f),
                        Float3(-sin(offset), 0f, cos(offset))
                    )

                    override fun update(delta: Float) {
                        parent.position = rot * parent.position
                        parent.scale = Float3(0.5f + sin(offsetS + getTimePassed()))
                    }
                }
            }
        }

        LightManager.calculateLightIndex(camera.pos)

        root.addChild {
            addComponent(DefaultShader())
            addComponent(getMesh("dragon"))
            id = "dragon"
            scale = Float3(.1f)
            addComponent(Rotator(20f,Float3(0f,1f,0f)))
        }

        root.addChild {
            addComponent(
                DefaultShader(
                    diffTexID = getTex("container_diff"),
                    specTexID = getTex("container_spec")
                )
            )
            addComponent(Data.getMesh("box"))
            position = Float3(-2f, 1.5f, 0f)
        }

        fun rColor() = normalize(Float3(rand.nextFloat(),rand.nextFloat(),rand.nextFloat()))
        fun rVector() = normalize(Float3(-0.5f+rand.nextFloat(),-0.5f+rand.nextFloat(),-0.5f+rand.nextFloat()))

        val armPosition = Float3(5f,1f,2f)
        val jointSize = Float3(0.25f)
        val jointMaterial = DefaultShader(Float3(0.4f,0.4f,0.4f))
        val armLength = 1f
        val armScale = Float3(0.15f,armLength,0.15f)
        val armMaterial = DefaultShader(Float3(0.7f,0.7f,1f))
        val joints = (0..8).map{Rotator(-20f + rand.nextFloat()*40f,rVector())}
        val base = Entity().apply{
            position = armPosition
        }
        var entitySelect = base
        joints.withIndex().forEach{
            val joint = Entity().apply{
                addComponent(it.value)
                addComponent(getMesh("sphere"))
                addComponent(jointMaterial)
                scale = jointSize
                position += Float3(0f,armLength,0f)
                id = "joint${it.index}"
            }
            val arm = Entity().apply{
                addComponent(armMaterial)
                addComponent(getMesh("cube"))
                scale = armScale
                position += Float3(0f,armLength,0f)
            }
            joint.addChild(arm)
            entitySelect.addChild(joint)
            entitySelect = arm
        }
        root.addChild(base)

        //LightManager.createSunLamp()
        LightManager.sunLamp?.direction = normalize(Float3(-0.5f + rand.nextFloat(), -1f, -0.5f + rand.nextFloat()))
    }

    override fun mouseDown(button: Int, action: Int, mousex: Float, mousey: Float) {
        if (button == 0 && action == 0) {
            mouseGrabbed = !mouseGrabbed
        }
    }

    override fun mouseMoved(mouseX: Float, mouseY: Float, deltaX: Float, deltaY: Float) {}
    override fun mouseScrolled(delta: Float) {}
    //key is case sensative, only normal typing keys, action 1 - pressed, 2 - held (repeats), 0 - released
    override fun keyEvent(key: String, action: Int) {
        if (key == "f" && action == 1){
            val lights = root.getAllEntityById("Light")
            lights.forEach{
                it.getComponentByType<SolidColor>()!!.color += Float3(0.05f)
            }
        }
    }

    override fun update(delta: Float, deltax: Float, deltay: Float) {
        setTitle("$windowTitle ${getFPS()}")

        if (keyPressed(GLFW_KEY_W)) camera.forward(delta)
        if (keyPressed(GLFW_KEY_S)) camera.backword(delta)
        if (keyPressed(GLFW_KEY_A)) camera.right(delta)
        if (keyPressed(GLFW_KEY_D)) camera.left(delta)
        if (keyPressed(GLFW_KEY_SPACE)) camera.up(delta)
        if (keyPressed(GLFW_KEY_LEFT_CONTROL)) camera.down(delta)


        root.update(delta)
        draw.update()
        camera.updateCamera(deltax, deltay, delta)
        LightManager.calculateLightIndex(camera.pos)
    }

    override fun draw(renderer: Renderer) {
        renderer.render(root)
        renderer.render(draw.entity)
    }
}

class VectorDraw {
    val mesh = Mesh()
    val entity = Entity()

    init {
        mesh.verts.add(Float3())
        mesh.verts.add(Float3())
        mesh.type = GL_LINES
    }

    fun init() {
        entity.addComponent(mesh).addComponent(SolidColor())
    }

    fun addLine(end: Float3) {
        mesh.verts.add(Float3())
        mesh.verts.add(end)
    }

    fun addLine(start: Float3, end: Float3) {
        mesh.verts.add(start)
        mesh.verts.add(end)
    }

    fun extendLine(end: Float3) {
        mesh.verts.add(mesh.verts.last())
        mesh.verts.add(end)
    }

    fun update() {
        mesh.updateMesh(true)
    }
}


class Mover(var newPos: Float3, var duration: Float,var startTime: Float = 0f): ComponentImpl(){
    var counter = 0f
    lateinit var oldPos: Float3

    override fun init(){
        val test = parent.getComponentByType<Mover>()
        if (test != null) {
            println("Warning, this will override previous Mover")
        }
        oldPos = parent.position
    }

    override fun update(delta: Float) {
        if (counter < startTime + duration) {
            counter += delta
            if (counter > startTime) {
                val timeElapsed = counter - startTime
                val completion = timeElapsed / duration
                parent.position = oldPos + (newPos - oldPos) * completion
            }
        }else{
            remove = true
        }
    }
}