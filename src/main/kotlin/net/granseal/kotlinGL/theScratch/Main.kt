package net.granseal.kotlinGL.theScratch

import net.granseal.kotlinGL.engine.*
import net.granseal.kotlinGL.engine.math.*
import net.granseal.kotlinGL.engine.renderer.Renderer
import net.granseal.kotlinGL.engine.shaders.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL33.*
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

fun main(args: Array<String>) {
    val main = Main(1600, 900, "KotlinGL", false)
    main.run()
}

class Main(width: Int, height: Int, title: String,fullScreen: Boolean) : KotlinGL(width, height, title,fullScreen) {

    var entities: MutableList<Entity> = mutableListOf()
    var selected = 0
    var moveBoxes = false
    val rand = Random(System.nanoTime())

    lateinit var bi: DynamicTexture
    lateinit var g2d: Graphics2D
    lateinit var light: PointLight
    private lateinit var lightEntity: Entity

    lateinit var floor: Entity
    lateinit var depth: Entity
    lateinit var flatCube: Mesh
    var vecDraw = VectorDraw()

    override fun initialize() {

        bi = DynamicTexture(256,256)
        g2d = bi.createGraphics()

        g2d.color = Color.DARK_GRAY
        g2d.fillRect(0,0,bi.width,bi.height)
        g2d.color = Color.green
        g2d.drawString("Hello World",5,30)

        camera.setPerspective(45f, width.toFloat() / height.toFloat(), 0.1f, 100f)

        flatCube = MeshManager.loadObj("flatcube.obj")

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

        val points = Mesh()
        (1..250).forEach{
            points.verts += -50 + rand.nextFloat() * 100
            points.verts += -50 + rand.nextFloat() * 100
            points.verts += -50 + rand.nextFloat() * 100
        }
        points.type = GL_LINES

        entities.add(Entity().addComponent(points)
                             .addComponent(SolidColor())
                             .addComponent(object: ComponentImpl(){

            override fun update(delta: Float) {
                parent.rotate(delta*15f,0.5f,1f,-0.25f)
            }
        }))

        depth = Entity().addComponent(Sprite(renderer.shadowMap.texID))
            .addComponent(quad)
            .apply { position = Vector3f(0f, 3f, 0f) }

        entities.add(depth)

        entities.add(Entity().addComponent(MeshManager.loadObj("deca2.obj"))
            .addComponent(DefaultShader(diffuse = Vector3f(0.5f,0.8f,0.3f)))
            .addComponent(object: ComponentImpl(){
                override fun update(delta: Float) {
                    parent.rotate(delta*75,0.5f,0.5f,0.5f)
                }
            }).apply { position = Vector3f(0f,8f,0f) })

        lightEntity = Entity().addComponent(SolidColor())
                              .addComponent(flatCube)
            .addComponent(PointLight().apply { linear = 0.5f })
            .apply { position = Vector3f(1.2f,1f,2f) }

        floor = Entity().addComponent(DefaultShader(diffuse = Vector3f(1f,1f,1f)))
                        .addComponent(MeshManager.loadObj("ground.obj"))
            .apply { position = Vector3f(0f,-5f,0f) }


        (1..5).forEach{
            val e = Entity()
            val p = PointLight()
            e.position.x = -20f + it*5 + rand.nextFloat()
            e.position.z = rand.nextDouble(-20.0,20.0).toFloat()
            e.position.y = rand.nextDouble(1.0,5.0).toFloat()
            p.diffuse.x = rand.nextFloat()
            p.diffuse.y = rand.nextFloat()
            p.diffuse.z = rand.nextFloat()
            p.diffuse.normalize()
            p.ambient = p.diffuse
            p.specular = p.diffuse
            p.linear = 0.5f
            e.addComponent(SolidColor(p.diffuse))
            e.addComponent(flatCube)
            e.addComponent(p)
            e.scale =  0.2f
            entities.add(e)
            e.addComponent(object: ComponentImpl(){
                val offset = rand.nextFloat()
                override fun update(delta: Float) {
                    parent.scale = 0.5f +  sin(offset + getTimePassed())
                }
            }).addComponent(object: ComponentImpl(){
                val offset = rand.nextFloat()*0.0025f
                val rot = Matrix3f(Vector3f(cos(offset),0f,sin(offset)),
                Vector3f(0f,1f,0f),
                Vector3f(-sin(offset),0f,cos(offset)))

                override fun update(delta: Float) {
                    parent.position = rot.multiply(parent.position)
                }
            })
        }

        LightManager.calculateLightIndex(camera.pos)

        lightEntity.scale = 0.1f
        entities.add(lightEntity)
        entities.add(floor)

        entities.add(Entity().addComponent(DefaultShader()).addComponent(MeshManager.loadObj("dragon.obj"))
            .apply { scale = 0.1f })
        entities.add(Entity().addComponent(DefaultShader(diffTexID = TextureManager.loadBufferedImage(bi)))
            .addComponent(flatCube).apply { position(2f, 1f, 2f) })
        entities.add(Entity().addComponent(
            DefaultShader(
                diffTexID = TextureManager.loadGLTexture("container2.png"),
                specTexID = TextureManager.loadGLTexture("container2_specular.png")
            )
        )
            .addComponent(flatCube).apply { position(-2f, 1.5f, 0f) })


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
            if (key == "p"){

            }
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
        val radius = 5f
        lightEntity.position.x = sin(getTimePassed() * 0.5f) * radius
        lightEntity.position.z = cos(getTimePassed() * 0.5f) * radius


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

        entities.forEach{it.update(delta)}

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
                    mat!!.tint = Vector3f(0.1f, 0.3f, 0.4f)
                } else {
                    mat!!.tint = Vector3f(0f, 0f, 0f)
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
    init {
        mesh.type = GL_LINES
    }

    operator fun FloatArray.plusAssign(vec: Vector3f){
        this.plus(vec.x)
        this.plus(vec.y)
        this.plus(vec.z)
    }

    fun addLine(end: Vector3f){
        mesh.verts += Vector3f(0f,0f,0f)
        mesh.verts += end
    }
    fun addLine(start: Vector3f, end: Vector3f){
        mesh.verts += start
        mesh.verts += end
    }
    fun update(){
        mesh.updateMesh()
    }
    fun getEntity(): Entity{
        return Entity().addComponent(mesh).addComponent(SolidColor())
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