package net.granseal.kotlinGL.engine

import net.granseal.kotlinGL.engine.math.Vector2f
import net.granseal.kotlinGL.engine.math.Vector3f
import java.io.File



class Mesh: ComponentImpl() {
    var verts = floatArrayOf()
    var normals = floatArrayOf()
    var textureCoords = floatArrayOf()
    var indices = intArrayOf()
    var type: Int = 4
    var colors = floatArrayOf()

    var vao: VAO? = null

    override fun init(){
        if (vao == null){
            vao = BufferManager.createVAOFromMesh(this)
        }
    }

    override fun draw() {
        vao?.draw()
    }

    fun getCombinedFloatArray(): FloatArray {
        return MeshManager.getCombinedFloatArray(verts,normals,textureCoords)
    }
}

object MeshManager {

    private var texI = 0
    fun getCombinedFloatArray(
        verts: FloatArray,
        normals: FloatArray,
        textureCoords: FloatArray
    ): FloatArray {
        texI = 0
        val result = mutableListOf<Float>()
        for (i in 0 until verts.size step 3) {
            result += verts.slice(i..i + 2)

            if (normals.isNotEmpty()) {
                result += normals.slice(i..i + 2)
                if (textureCoords.isNotEmpty()) {
                    result += textureCoords.slice(texI..texI + 1)

                    texI += 2
                }
            }
        }
        return result.toFloatArray()
    }

    fun loadObj(file: String): Mesh {
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
            if (line.isEmpty())return@forEach
            if (line[0] == "v") {
                var s = 1
                while (line[s].isEmpty())s++
                vertRef += Vector3f(
                    line[s+0].toFloat(),
                    line[s+1].toFloat(),
                    line[s+2].toFloat()
                )
            }
            if (line[0] == "vt") {
                texRef += Vector2f(line[1].toFloat(), line[2].toFloat())
            }
            if (line[0] == "vn") {
                normalRef += Vector3f(
                    line[1].toFloat(),
                    line[2].toFloat(),
                    line[3].toFloat()
                )
            }
            if (line[0] == "f") {
                val f1 = line[1].split("/")
                val f2 = line[2].split("/")
                val f3 = line[3].split("/")
                iRef += f1[0].toInt()
                vertActual += vertRef[f1[0].toInt() - 1].x
                vertActual += vertRef[f1[0].toInt() - 1].y
                vertActual += vertRef[f1[0].toInt() - 1].z
                if (f1.size > 1 && f1[1].isNotEmpty()) {
                    texActual += texRef[f1[1].toInt() - 1].x
                    texActual += texRef[f1[1].toInt() - 1].y
                }
                if (f1.size > 2) {
                    norActual += normalRef[f1[2].toInt() - 1].x
                    norActual += normalRef[f1[2].toInt() - 1].y
                    norActual += normalRef[f1[2].toInt() - 1].z
                }

                iRef += f2[0].toInt()
                vertActual += vertRef[f2[0].toInt() - 1].x
                vertActual += vertRef[f2[0].toInt() - 1].y
                vertActual += vertRef[f2[0].toInt() - 1].z
                if (f2.size > 1 && f2[1].isNotEmpty()) {
                    texActual += texRef[f2[1].toInt() - 1].x
                    texActual += texRef[f2[1].toInt() - 1].y
                }
                if (f2.size > 2) {
                    norActual += normalRef[f2[2].toInt() - 1].x
                    norActual += normalRef[f2[2].toInt() - 1].y
                    norActual += normalRef[f2[2].toInt() - 1].z
                }

                iRef += f3[0].toInt()
                vertActual += vertRef[f3[0].toInt() - 1].x
                vertActual += vertRef[f3[0].toInt() - 1].y
                vertActual += vertRef[f3[0].toInt() - 1].z
                if (f3.size > 1 && f3[1].isNotEmpty()) {
                    texActual += texRef[f3[1].toInt() - 1].x
                    texActual += texRef[f3[1].toInt() - 1].y
                }
                if (f3.size > 2) {
                    norActual += normalRef[f3[2].toInt() - 1].x
                    norActual += normalRef[f3[2].toInt() - 1].y
                    norActual += normalRef[f3[2].toInt() - 1].z
                }
            }

        }
        println("Loaded Mesh: $file")
        println("Verticies loaded: ${vertActual.size / 3}")
        println("Normals loaded: ${norActual.size / 3}")
        println("Texture Coords Loaded: ${texActual.size / 2}")
        return Mesh().apply {
            verts = vertActual.toFloatArray()
            normals = norActual.toFloatArray()
            textureCoords = texActual.toFloatArray()
        }
    }
}