package net.granseal.kotlinGL.theScratch

import com.curiouscreature.kotlin.math.*
import net.granseal.kotlinGL.engine.*
import net.granseal.kotlinGL.engine.renderer.Renderer
import net.granseal.kotlinGL.engine.shaders.*
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL33.*
import java.awt.image.BufferedImage
import java.io.File
import javax.script.ScriptContext
import javax.script.ScriptEngineFactory
import javax.script.ScriptEngineManager
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

fun main(args: Array<String>) {
    val main = Main(1600, 900, "KotlinGL", false)

    val js = ScriptEngineManager().getEngineByExtension("js")
    val b = js.createBindings()
    b.put("a", 10)
    b.put("b", 20)
    b.put("c", 30)
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

    fun loadSaveFile(g: SaveFile): List<Entity> {
        val list = mutableListOf<Entity>()
        g.objects.forEach {
            val e = Entity()
            when (it.type) {
                "Box" -> e.addComponent(Data.getMesh("box"))
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

        val g = SaveFile()
        with(g.objects) {
            add(KglObject("Box", color = rColor()))
            add(KglObject("Sphere", color = rColor()))
        }

        camera.projection = perspective(45f, width.toFloat() / height.toFloat(), 0.1f, 200f)

        root.addChild(Entity().apply {
            addComponent(Sprite(renderer.shadowMap.texID))
            addComponent(Data.getMesh("quad"))
            position = Float3(0f, 3f, 0f)
        })

        root.addChild(Entity().apply {
            addComponent(Data.getMesh("deca"))
            addComponent(DefaultShader(diffuse = Float3(0.5f, 0.8f, 0.3f)))
            position = Float3(0f, 8f, 0f)
            scale = float3(.5f)
            addComponent(object : ComponentImpl() {
                override fun update(delta: Float) {
                    parent.rotate(delta * 75, 0.5f, 0.5f, 0.5f)
                }
            })
        })

        root.addChild(Entity().apply {
            addComponent(
                DefaultShader(
                    diffTexID = Data.getTex("ground"),
                    shininess = 0.1f
                )
            )
            addComponent(Data.getMesh("terrain"))
            position = Float3(-40f, -100f, -40f)
        })


        repeat(4) {
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
            addComponent(Data.getMesh("dragon"))
            scale = float3(0.1f)
        })

        root.addChild(Entity().apply {
            addComponent(
                DefaultShader(
                    diffTexID = Data.getTex("container_diff"),
                    specTexID = Data.getTex("container_spec")
                )
            )
            addComponent(Data.getMesh("box"))
            position(-2f, 1.5f, 0f)
        })

        class Rotator(var angle: Float, var x: Float, var y: Float, var z: Float) : ComponentImpl() {
            override fun update(delta: Float) {
                parent.rotate(angle * delta, x, y, z)
            }
        }

        root.addChild(Entity().apply {
            addComponent(Rotator(25f, 1f, 0f, 0f))
            move(5f, 0.5f, 2f)
            rotate(90f, 0f, 0f, 1f)
        })
        root.children.last().addChild(Entity().apply {
            addComponent(DefaultShader(Float3(0f, 1f, 0f)))
            addComponent(getMesh("cube"))
            scale = Float3(0.25f, 1f, 0.25f)
            move(0f, 1f, 0f)
        }).addChild(Entity().apply {
            addComponent(Rotator(25f, 0f, 0f, 1f))
            move(0f, 1f, 0f)
        }).addChild(Entity().apply {
            addComponent(DefaultShader(Float3(0f, 0f, 1f)))
            addComponent(getMesh("cube"))
            scale = Float3(0.25f, 1f, 0.25f)
            move(0f, 1f, 0f)
        }).addChild(Entity().apply {
            addComponent(Rotator(25f, 1f, 0f, 0f))
            move(0f, 1f, 0f)
        }).addChild(Entity().apply {
            addComponent(DefaultShader(Float3(1f, 1f, 1f)))
            addComponent(getMesh("cube"))
            move(0f, 1f, 0f)
            scale = Float3(0.25f, 1f, 0.25f)
        }).addChild(Entity().apply {
            addComponent(Rotator(25f, 0f, 0f, 1f))
            move(0f, 1f, 0f)
        }).addChild(Entity().apply {
            addComponent(SolidColor(Float3(0f, 1f, 1f)))
            addComponent(getMesh("cube"))
            addComponent(PointLight())
            move(0f, 1f, 0f)
            scale = Float3(0.25f, 1f, 0.25f)
            addComponent(object : ComponentImpl() {
                var counter = 0f
                var timeToDraw = 0.5f
                override fun update(delta: Float) {
                    counter += delta
                    if (counter >= timeToDraw){
                        counter = 0f
                        val pos = (transform() * scale() * (Float4(position, 1f) + Float3(0f, -2f, 0f)))
                        draw.extendLine(pos.xyz)
                    }
                }
            })
        })


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

class VectorDraw() {
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
