package net.granseal.kotlinGL.theScratch

import net.granseal.kotlinGL.engine.*
import net.granseal.kotlinGL.engine.math.Matrix3f
import net.granseal.kotlinGL.engine.math.Vector3f
import net.granseal.kotlinGL.engine.shaders.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL33
import java.awt.Color
import java.awt.Graphics2D
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

    val rand = Random(System.nanoTime())
    lateinit var bi: DynamicTexture
    lateinit var g2d: Graphics2D

    lateinit var light: PointLight

    private lateinit var lightEntity: Entity
    lateinit var floor: Entity

    override fun initialize() {
        bi = DynamicTexture(512,512)
        g2d = bi.createGraphics()

        g2d.color = Color.DARK_GRAY
        g2d.fillRect(0,0,512,512)
        g2d.color = Color.green
        g2d.drawString("Hello World",0,30)

        camera.setPerspective(45f, width.toFloat() / height.toFloat(), 0.1f, 100f)
        //ImageIO.write(bi,"png", File("test.png"))

        val flatCube = MeshManager.loadObj("flatcube.obj")

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

        quad.type = GL33.GL_TRIANGLE_FAN

        entities.add(Entity().addComponent(Sprite(renderer.shadowMap.texID))
                             .addComponent(quad)
                             .apply { position = Vector3f(0f,3f,0f)})



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



        SunLamp()
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

//        g2d.color = Color.WHITE
//        g2d.fillRect(0,0,bi.width,bi.height)
//        g2d.color = Color.BLACK
//        g2d.drawOval((256+sin(getTimePassed())*128).toInt(),(256+cos(getTimePassed())*128).toInt(),64,64)
//        bi.updateTexture()

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