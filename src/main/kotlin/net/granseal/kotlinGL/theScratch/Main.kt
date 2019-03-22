package net.granseal.kotlinGL.theScratch

import com.curiouscreature.kotlin.math.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.granseal.kotlinGL.engine.*
import net.granseal.kotlinGL.engine.renderer.Renderer
import net.granseal.kotlinGL.engine.shaders.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL33.*
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.File
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import com.google.gson.reflect.TypeToken



fun main(args: Array<String>) {
    val main = Main(1600, 900, "KotlinGL", false)
    main.run()
}

data class KglObject(
    val type: String,
    val position: Float3 = Float3(0f,0f,0f),
    val scale: Float = 1f,
    val color: Float3 = Float3(1f,1f,1f)
)
data class KglResource(
    val type: String,
    val name: String,
    val value: String
)
class SaveFile {
    var resources = mutableListOf<KglResource>()
    var objects = mutableListOf<KglObject>()
}

fun getMesh(name: String) = Data.getMesh(name)
fun getTex(name: String) = Data.getTex(name)
fun loadResourceFile(f: String) = Data.loadResourceFile(f)

object Data {
    val gson = GsonBuilder().setPrettyPrinting().create()
    val meshes = mutableMapOf<String,Mesh>()
    val textures = mutableMapOf<String,Int>()

    fun loadMesh(name: String, value: String){
        meshes.putIfAbsent(name,MeshManager.loadObj(value))
    }
    fun loadResourceFile(f: String){
        val collectionType = object : TypeToken<List<KglResource>>() {}.type
        val resources = gson.fromJson<List<KglResource>>(File(f).readText(),collectionType)
        resources.forEach { Data.loadResource(it) }
    }
    fun loadResource(r: KglResource){
        when (r.type){
            "mesh"      -> loadMesh(r.name,r.value)
            "texture"   -> loadTexture(r.name,r.value)
            else        -> throw Exception("${r.type}, is not a valid resource")
        }
    }
    fun loadTexture(name: String, value: String){
        textures.putIfAbsent(name,TextureManager.loadGLTexture(value))
    }
    fun getMesh(name: String): Mesh {
        val m = meshes[name]
        if (m != null)return m else throw Exception("No Mesh of that name")
    }

    fun getTex(name: String): Int {
        val id = textures[name]
        if (id != null)return id else throw Exception("No Texture by that name loaded")
    }

    init {
        val quad = Mesh()
        quad.verts = floatArrayOf(
            -1.0f,-1.0f,0.0f,
            -1.0f,1.0f,0.0f,
            1.0f,1.0f,0.0f,
            1.0f,-1.0f,0.0f
        )
        quad.normals = floatArrayOf(
            1f,0f,0f,
            1f,0f,0f,
            1f,0f,0f,
            1f,0f,0f
        )
        quad.textureCoords = floatArrayOf(
            1f,1f,
            1f,0f,
            0f,0f,
            0f,1f
        )
        quad.type = GL_TRIANGLE_FAN
        meshes.putIfAbsent("quad",quad)
    }
}

class Main(width: Int, height: Int, title: String,fullScreen: Boolean) : KotlinGL(width, height, title,fullScreen) {
    var entities: MutableList<Entity> = mutableListOf()
    var selected = 0
    var moveBoxes = false
    val rand = Random(System.nanoTime())

    lateinit var flatCube: Mesh


    fun rColor() = Float3(rand.nextFloat(),rand.nextFloat(),rand.nextFloat())

    fun loadSaveFile(g: SaveFile): List<Entity> {
        val list = mutableListOf<Entity>()
        g.objects.forEach {
            val e = Entity()
            when (it.type){
                "Box" -> e.addComponent(Data.getMesh("box"))
            }
            e.position = it.position
            e.scale = it.scale
            e.addComponent(SolidColor(it.color))
            list += e
        }
        return list
    }

    override fun initialize() {
        loadResourceFile("resources.kgl")

        val g = SaveFile()
        with (g.objects){
            add(KglObject("Box", color = rColor()))
            add(KglObject("Sphere", color = rColor()))
        }


        camera.projection = perspective(45f, width.toFloat() / height.toFloat(), 0.1f, 200f)

        flatCube = Data.getMesh("box")



/*
val points = Mesh()
(1..250).forEach{
points.verts += -50 + rand.nextFloat() * 100
points.verts += -50 + rand.nextFloat() * 100
points.verts += -50 + rand.nextFloat() * 100
}
points.type = GL_LINES

entities.add(Entity().apply {
addComponent(points)
addComponent(SolidColor())
addComponent(object : ComponentImpl() {
override fun update(delta: Float) {
parent.rotate(delta * 15f, 0.5f, 1f, -0.25f)
}
})
})
*/

        entities.add(Entity().apply{
            addComponent(Sprite(renderer.shadowMap.texID))
            addComponent(Data.getMesh("quad"))
            position = Float3(0f, 3f, 0f)
        })

        entities.add(Entity().apply {
            addComponent(Data.getMesh("deca"))
            addComponent(DefaultShader(diffuse = Float3(0.5f,0.8f,0.3f) ))
            position = Float3(0f,8f,0f)
            scale = 0.5f
            addComponent(object: ComponentImpl(){
                override fun update(delta: Float) {
                    parent.rotate(delta*75,0.5f,0.5f,0.5f)
                }
            })
        })

        entities.add(Entity().apply {
            addComponent(
                DefaultShader(
                    diffTexID = Data.getTex("ground"),
                    shininess = 0.1f
                )
            )
            addComponent(Data.getMesh("terrain"))
            position = Float3(-40f, -101f, -40f)
        })


        repeat(10){
            val e = Entity()
            val p = PointLight()
            e.position.x = -20f + it*5 + rand.nextFloat()
            e.position.z = rand.nextDouble(-80.0,80.0).toFloat()
            e.position.y = rand.nextDouble(3.0,15.0).toFloat()
            p.diffuse = Float3(1f,1f,1f)
            p.ambient = p.diffuse * 0.1f
            p.specular = p.diffuse
            p.linear = 0.25f
            e.addComponent(SolidColor(p.diffuse))
            e.addComponent(flatCube)
            e.addComponent(p)
            e.scale =  0.2f
            entities.add(e)
            e.addComponent(object: ComponentImpl(){
                val offsetS = rand.nextFloat()
                val offset = rand.nextFloat()*0.0025f
                val rot = Mat3(Float3(cos(offset),0f,sin(offset)),
                    Float3(0f,1f,0f),
                    Float3(-sin(offset),0f,cos(offset)))

                override fun update(delta: Float) {
                    parent.position = rot * parent.position
                    parent.scale = 0.5f +  sin(offsetS + getTimePassed())
                }
            })
        }

        LightManager.calculateLightIndex(camera.pos)

        entities.add(Entity().apply {
            addComponent(DefaultShader())
            addComponent(Data.getMesh("dragon"))
            scale = 0.1f
        })

        entities.add(Entity().apply{
            addComponent(
                DefaultShader(
                    diffTexID = Data.getTex("container_diff"),
                    specTexID = Data.getTex("container_spec")
                )
            )
            addComponent(Data.getMesh("box"))
            position(-2f, 1.5f, 0f)
        })

        entities.add(Entity().apply {
            addComponent(DefaultShader(Float3(0f,0f,0f)))
            addComponent(Data.getMesh("box"))
            move(5f,2f,2f)
            scale = 0.75f
        })
        entities.last().addChild(Entity().apply {
            addComponent(DefaultShader(Float3(1f,0f,0f)))
            addComponent(Data.getMesh("box"))
            move(0f,2f,0f)
            rotate(45F,0f,0f,1f)
            scale = 0.75f
        }).addChild(Entity().apply {
            addComponent(DefaultShader(Float3(0f,0f,0f)))
            addComponent(Data.getMesh("box"))
            move(0f,2f,0f)
            rotate(45F,1f,0f,0f)
            scale = 0.75f
        }).addChild(Entity().apply {
            addComponent(DefaultShader(Float3(1f,1f,0f)))
            addComponent(Data.getMesh("box"))
            rotate(45F,1f,0f,0f)
            move(0f,2f,0f)
            scale = 0.75f
        }).addChild(Entity().apply {
            addComponent(DefaultShader(Float3(0f,1f,1f)))
            addComponent(Data.getMesh("box"))
            move(0f,2f,0f)
            rotate(45F,1f,0f,0f)
            scale = 0.75f
        })


        LightManager.createSunLamp()
    }

    override fun mouseClicked(button: Int,action: Int, mousex: Float, mousey: Float) {
        if (button == 0 && action == 0){
            mouseGrabbed = !mouseGrabbed
        }
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
        }

        entities.forEach{it.update(delta)}
//        val newP = vecDraw.getLastPos() + Vector3f(-0.05f + rand.nextFloat()*0.1f,-0.049f + rand.nextFloat()*0.1f,-0.05f + rand.nextFloat()*0.1f)
//        vecDraw.extendLine(newP)
//        vecDraw.update()
        camera.updateCamera(deltax,deltay,delta)
        LightManager.calculateLightIndex(camera.pos)
    }

    override fun draw(renderer: Renderer) {
        var mat: DefaultShader?
        var mat2: Sprite?

        entities.withIndex().forEach {
            mat = it.value.components().singleOrNull{it is DefaultShader} as DefaultShader?
            mat2 = it.value.components().singleOrNull{it is Sprite} as Sprite?
            if (mat != null){
                if (it.index == selected) {
                    mat!!.tint = Float3(0.1f, 0.3f, 0.4f)
                } else {
                    mat!!.tint = Float3(0f, 0f, 0f)
                }
            }
            if (mat2 == null)renderer.render(it.value)
        }
        entities.forEach{
            mat2 = it.components().singleOrNull(){it is Sprite} as Sprite?
            if (mat2 != null){
                renderer.render(it)
            }
        }
    }
}



class VectorDraw(){
    val mesh = Mesh()
    val entity = Entity()

    init {
        mesh.verts += floatArrayOf(0f,0f,0f,0f,0f,0f)
        mesh.type = GL_LINES
    }

    fun init(){
        entity.addComponent(mesh).addComponent(SolidColor())

    }
    fun addLine(end: Float3){
        mesh.verts += floatArrayOf(0f,0f,0f)
        mesh.verts += floatArrayOf(end.x,end.y,end.z)
    }
    fun addLine(start: Float3, end: Float3){
        mesh.verts += floatArrayOf(start.x,start.y,start.z)
        mesh.verts += floatArrayOf(end.x,end.y,end.z)
    }
    fun extendLine(end: Float3){
        mesh.verts += mesh.verts.takeLast(3)
        mesh.verts += floatArrayOf(end.x,end.y,end.z)
    }
    fun update(){
        mesh.updateMesh(true)
        println(mesh.verts.size)
    }
    fun getLastPos():Float3{
        val vert = mesh.verts.takeLast(3)
        return Float3(vert[0],vert[1],vert[2])
    }
}

class DynamicTexture(width: Int, height: Int): BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB) {
    var texID: Int? = null

    fun updateTexture(): Int {
        if (texID == null) {
            texID = TextureManager.loadBufferedImage(this)
            return texID!!
        } else {
            TextureManager.updateTexture(this, texID!!)
            return texID!!
        }
    }
}