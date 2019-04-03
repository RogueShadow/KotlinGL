package net.granseal.kotlinGL.theScratch

import com.curiouscreature.kotlin.math.*
import net.granseal.kotlinGL.engine.*
import net.granseal.kotlinGL.engine.components.ComponentImpl
import net.granseal.kotlinGL.engine.renderer.Renderer
import net.granseal.kotlinGL.engine.shaders.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL33.*
import javax.script.ScriptContext
import javax.script.ScriptEngineManager
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

fun main(args: Array<String>) {
    val main = Main(1600, 900, "KotlinGL", false)

    val js = ScriptEngineManager().getEngineByExtension("js")
    val b = js.createBindings()
    b["a"] = 10
    b["b"] = 20
    b["c"] = 30
    js.setBindings(b, ScriptContext.GLOBAL_SCOPE)
    js.eval("var d = a + b + c;")
    val test = js["d"]
    println(test) // 60


    main.run()
}

class Main(width: Int, height: Int, title: String, fullScreen: Boolean) : KotlinGL(width, height, title, fullScreen) {
    val root = Entity()
    val draw = VectorDraw()
    val rand = Random(System.nanoTime())

    fun rColor() = Float3(rand.nextFloat(), rand.nextFloat(), rand.nextFloat())
    fun float3(init: Float) = Float3(init, init, init)

    fun loadSaveFile(g: Data.SaveFile): List<Entity> {
        val list = mutableListOf<Entity>()
        g.objects.forEach {
            val e = Entity()
            when (it.type) {
                "Box" -> e.addComponent(getMesh("box"))
            }
            e.position = it.position
            e.scale = float3(it.scale)
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

        root.addChild(Entity().apply {
            addComponent(Sprite(renderer.shadowMap.texID))
            addComponent(getMesh("quad"))
            position = Float3(0f, 3f, 0f)
        })

        root.addChild(Entity().apply {
            addComponent(getMesh("deca"))
            addComponent(DefaultShader(diffuse = Float3(0.5f, 0.8f, 0.3f)))
            position = Float3(0f, 8f, 0f)
            scale = float3(.5f)
            addComponent(object : ComponentImpl() {
                override fun update(delta: Float) {
                    parent.rotation *= rotation(Float3(0.5f, 0.5f, 0.5f),delta * 75)
                }
            })
        })

        root.addChild(Entity().apply {
            addComponent(
                DefaultShader(
                    diffTexID = getTex("ground"),
                    shininess = 0.1f
                )
            )
            addComponent(getMesh("terrain"))
            position = Float3(-40f, -100f, -40f)
        })


        repeat(0) {
            val e = Entity()
            val p = PointLight()
            e.position.x = -20f + it * 5 + rand.nextFloat()
            e.position.z = rand.nextDouble(-80.0, 80.0).toFloat()
            e.position.y = rand.nextDouble(3.0, 15.0).toFloat()
            p.diffuse = Float3(1f, 1f, 1f)
            p.ambient = p.diffuse * 0.1f
            p.specular = p.diffuse
            p.linear = 0.25f
            e.addComponent(SolidColor(p.diffuse))
            e.addComponent(getMesh("box"))
            e.addComponent(p)
            e.scale = float3(0.2f)
            root.addChild(e)
            e.addComponent(object : ComponentImpl() {
                val offsetS = rand.nextFloat()
                val offset = rand.nextFloat() * 0.0025f
                val rot = Mat3(
                    Float3(cos(offset), 0f, sin(offset)),
                    Float3(0f, 1f, 0f),
                    Float3(-sin(offset), 0f, cos(offset))
                )

                override fun update(delta: Float) {
                    parent.position = rot * parent.position
                    parent.scale = float3(0.5f + sin(offsetS + getTimePassed()))
                }
            })
        }

        LightManager.calculateLightIndex(camera.pos)

        root.addChild(Entity().apply {
            addComponent(DefaultShader())
            addComponent(getMesh("dragon"))
            scale = float3(0.1f)
        })

        root.addChild(Entity().apply {
            addComponent(
                DefaultShader(
                    diffTexID = getTex("container_diff"),
                    specTexID = getTex("container_spec")
                )
            )
            addComponent(Data.getMesh("box"))
            position = Float3(-2f, 1.5f, 0f)
        })

        fun rVec() = normalize(Float3(rand.nextFloat(),rand.nextFloat(),rand.nextFloat()))
        class Rotator(var angle: Float, var x: Float, var y: Float, var z: Float) : ComponentImpl() {
            constructor(angle: Float, axis: Float3) : this(angle,axis.x,axis.y,axis.z)
            override fun update(delta: Float) {
                parent.rotation *= rotation(Float3(x,y,z),angle * delta)
            }
        }
        val armPosition = Float3(5f,1f,2f)
        val jointSize = float3(0.25f)
        val jointMaterial = DefaultShader(Float3(0.4f,0.4f,0.4f))
        val armLength = 0.5f
        val armScale = Float3(0.15f,armLength,0.15f)
        val armMaterial = DefaultShader(Float3(0.7f,0.7f,1f))
        val joints = (0..8).map{Rotator(-20 + rand.nextFloat()*20f,rVec())}
        val base = Entity().apply{
            position = armPosition
        }
        var entitySelect = base
        joints.forEach{
            val joint = Entity().apply{
                addComponent(it)
                addComponent(getMesh("sphere"))
                addComponent(jointMaterial)
                scale = jointSize
                position += Float3(0f,armLength,0f)
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

        LightManager.createSunLamp()
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
