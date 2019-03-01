package net.granseal.kotlinGL.engine

import net.granseal.kotlinGL.engine.BaseMesh.Companion.MESH_COLORS
import net.granseal.kotlinGL.engine.BaseMesh.Companion.MESH_NORMALS
import net.granseal.kotlinGL.engine.BaseMesh.Companion.MESH_TEX
import net.granseal.kotlinGL.engine.BaseMesh.Companion.MESH_VERTS
import net.granseal.kotlinGL.engine.math.Vector2f
import net.granseal.kotlinGL.engine.math.Vector3f
import java.io.File
import kotlin.experimental.or

open class TexturedMesh(var textureCoords: FloatArray,
                        val normals: FloatArray,
                        override val verticies: FloatArray): BaseMesh{
    override lateinit var parent: Entity


    override fun updateCP(delta: Float) {

    }

    override fun drawCP() {
        draw()
    }

    override fun draw(){
        vao.draw(verticies.size/3)
    }

    override var vao = VAOManager.createVAOFromMesh(this)
    override fun getType() = MESH_VERTS or MESH_NORMALS or MESH_TEX
    override fun getCombinedFloatArray(): FloatArray {
        return MeshManager.getCombinedFloatArray(verticies,normals,textureCoords)
    }
}

open class ColoredMesh(var colors: FloatArray,
                       val normals: FloatArray,
                       override val verticies: FloatArray): BaseMesh {
    override lateinit var parent: Entity
    override fun updateCP(delta: Float) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun drawCP() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun draw() {

    }

    override var vao: VAO = VAOManager.createVAOFromMesh(this)
    override fun getCombinedFloatArray(): FloatArray {
        return MeshManager.getCombinedFloatArray(verticies,normals,colorCoords = colors)
    }
    override fun getType() = MESH_COLORS or MESH_VERTS or MESH_NORMALS
}

open class NormalMesh(var normals: FloatArray, override val verticies: FloatArray): BaseMesh{
    override lateinit var parent: Entity
    override fun updateCP(delta: Float) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun drawCP() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun draw() {

    }

    override var vao: VAO = VAOManager.createVAOFromMesh(this)

    override fun getCombinedFloatArray(): FloatArray {
        return MeshManager.getCombinedFloatArray(verticies,normals)
    }
    override fun getType(): Byte = MESH_VERTS or MESH_NORMALS
}

open class Mesh(override val verticies: FloatArray) : BaseMesh{
    override lateinit var parent: Entity
    override fun updateCP(delta: Float) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun drawCP() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun draw() {

    }

    override var vao: VAO = VAOManager.createVAOFromMesh(this)

    override fun getCombinedFloatArray(): FloatArray {
        return verticies
    }
    override fun getType(): Byte = MESH_VERTS
}

interface BaseMesh: Component{
    val verticies: FloatArray
    var vao: VAO
    companion object {
        const val MESH_VERTS: Byte   = 0b00000001
        const val MESH_NORMALS: Byte = 0b00000010
        const val MESH_COLORS: Byte  = 0b00000100
        const val MESH_TEX: Byte     = 0b00001000
        const val MESH_INDICES: Byte = 0b00010000
    }

  fun getType(): Byte
    fun getCombinedFloatArray(): FloatArray
    fun draw()
}

object MeshManager{

    private var texI = 0
    fun getCombinedFloatArray(
        verticies: FloatArray,
        normals: FloatArray? = null,
        textureCoords: FloatArray? = null,
        colorCoords: FloatArray? = null
    ): FloatArray {
        texI = 0
        val result = mutableListOf<Float>()
        for (i in 0 until verticies.size step 3) {
            result += verticies.slice(i..i + 2)

            if (normals != null) {
                result += normals.slice(i..i + 2)
                if (textureCoords != null && textureCoords.size > 0) {
                    result += textureCoords.slice(texI..texI + 1)

                    texI += 2
                }
                if (colorCoords != null) {
                    result += colorCoords.slice(i..i + 2)
                }
            }
        }
        return result.toFloatArray()
    }

    fun loadObj(file: String): BaseMesh {
        val obj = File(file).readLines()
        val vertRef = mutableListOf<Vector3f>()
        val texRef = mutableListOf<Vector2f>()
        val normalRef = mutableListOf<Vector3f>()
        val iRef = mutableListOf<Int>()
        val vertActual = mutableListOf<Float>()
        val texActual = mutableListOf<Float>()
        val norActual = mutableListOf<Float>()
        obj.forEach {
            val line = it.split(" ")
            if (line[0] == "v"){
                vertRef += Vector3f(
                    line[1].toFloat(),
                    line[2].toFloat(),
                    line[3].toFloat()
                )
            }
            if (line[0] == "vt"){
                texRef += Vector2f(line[1].toFloat(), line[2].toFloat())
            }
            if (line[0] == "vn"){
                normalRef += Vector3f(
                    line[1].toFloat(),
                    line[2].toFloat(),
                    line[3].toFloat()
                )
            }
            if (line[0] == "f"){
                val f1 = line[1].split("/")
                val f2 = line[2].split("/")
                val f3 = line[3].split("/")
                iRef += f1[0].toInt()
                vertActual += vertRef[f1[0].toInt()-1].x
                vertActual += vertRef[f1[0].toInt()-1].y
                vertActual += vertRef[f1[0].toInt()-1].z
                if (f1.size > 1 && f1[1].isNotEmpty()){
                    texActual += texRef[f1[1].toInt()-1].x
                    texActual += texRef[f1[1].toInt()-1].y
                }
                if (f1.size > 2){
                    norActual += normalRef[f1[2].toInt()-1].x
                    norActual += normalRef[f1[2].toInt()-1].y
                    norActual += normalRef[f1[2].toInt()-1].z
                }

                iRef += f2[0].toInt()
                vertActual += vertRef[f2[0].toInt()-1].x
                vertActual += vertRef[f2[0].toInt()-1].y
                vertActual += vertRef[f2[0].toInt()-1].z
                if (f2.size > 1 && f2[1].isNotEmpty()){
                    texActual += texRef[f2[1].toInt()-1].x
                    texActual += texRef[f2[1].toInt()-1].y
                }
                if (f2.size > 2){
                    norActual += normalRef[f2[2].toInt()-1].x
                    norActual += normalRef[f2[2].toInt()-1].y
                    norActual += normalRef[f2[2].toInt()-1].z
                }

                iRef += f3[0].toInt()
                vertActual += vertRef[f3[0].toInt()-1].x
                vertActual += vertRef[f3[0].toInt()-1].y
                vertActual += vertRef[f3[0].toInt()-1].z
                if (f3.size > 1 && f3[1].isNotEmpty()){
                    texActual += texRef[f3[1].toInt()-1].x
                    texActual += texRef[f3[1].toInt()-1].y
                }
                if (f3.size > 2){
                    norActual += normalRef[f3[2].toInt()-1].x
                    norActual += normalRef[f3[2].toInt()-1].y
                    norActual += normalRef[f3[2].toInt()-1].z
                }
            }

        }
        println("Loaded Mesh: $file")
        println("Verticies loaded: ${vertActual.size/3}")
        println("Normals loaded: ${norActual.size/3}")
        println("Texture Coords Loaded: ${texActual.size/2}")
        return TexturedMesh(
            textureCoords = texActual.toFloatArray(),
            verticies = vertActual.toFloatArray(),
            normals = norActual.toFloatArray())
    }
}