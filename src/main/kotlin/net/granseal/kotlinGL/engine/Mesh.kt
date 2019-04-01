package net.granseal.kotlinGL.engine

import com.curiouscreature.kotlin.math.Float2
import com.curiouscreature.kotlin.math.Float3
import java.io.File
import kotlin.streams.toList


class Mesh: ComponentImpl() {
    var combinedVerts = listOf<List<Float>>()
        get() {
            if (field.isEmpty()) {
                field = MeshManager.getCombinedFloatArray(verts,normals,textureCoords,colors)
            }
            return field
        }
    var verts = mutableListOf<Float3>()
    var normals = mutableListOf<Float3>()
    var textureCoords = mutableListOf<Float2>()
    var indices = intArrayOf()
    var type: Int = 4
    var colors = mutableListOf<Float3>()
    fun isIndexed(): Boolean = indices.isNotEmpty()

    var vao: VAO? = null

    override fun init(){
        if (vao == null){
            vao = BufferManager.createVAOFromMesh(this)
        }
    }

    fun updateMesh(resize: Boolean = false){
        BufferManager.updateVBO(this,resize)
    }

    override fun draw() {
        val vao = vao
        if (vao != null) BufferManager.draw(vao)
    }
}

object MeshManager {

    fun getCombinedFloatArray(
        verts: List<Float3>,
        normals: List<Float3> = emptyList(),
        tex: List<Float2> = emptyList(),
        colors: List<Float3> = emptyList()
    ): List<List<Float>>
    {
        fun Float3.toList() = listOf(this.x,this.y,this.z)
        fun Float2.toList() = listOf(this.x,this.y)
        val result = mutableListOf<List<Float>>()
        (0 until verts.size).forEach{ i ->
            val vert = mutableListOf<Float>()
            vert.addAll(verts[i].toList())
            if (normals.isNotEmpty()) vert.addAll(normals[i].toList())
            if (tex.isNotEmpty()    ) vert.addAll(tex[i].toList())
            if (colors.isNotEmpty() ) vert.addAll(colors[i].toList())
            result += vert
        }
        return result
    }

    fun loadObj(file: String): Mesh {
        val obj = File(file).readLines()
        val vertRef = mutableListOf<Float3>()
        val texRef = mutableListOf<Float2>()
        val normalRef = mutableListOf<Float3>()
        val vertActual = mutableListOf<Float3>()
        val texActual = mutableListOf<Float2>()
        val norActual = mutableListOf<Float3>()
        val indexes = mutableListOf<List<Int>>()
        obj.forEach {
            val line = it.split(" ")
            if (line.isEmpty())return@forEach
            if (line[0] == "v") {
                vertRef += Float3(
                    line[1].toFloat(),
                    line[2].toFloat(),
                    line[3].toFloat()
                )
            }
            if (line[0] == "vt") {
                texRef += Float2(line[1].toFloat(), line[2].toFloat())
            }
            if (line[0] == "vn") {
                normalRef += Float3(
                    line[1].toFloat(),
                    line[2].toFloat(),
                    line[3].toFloat()
                )
            }
            if (line[0] == "f") {
                val faces = listOf(line[1].split("/"),line[2].split("/"),line[3].split("/"))
                faces.forEach { face ->
                    indexes += listOf(
                        face[0].toInt(),
                        face[1].toInt(),
                        face[2].toInt())
                }
            }
        }
        indexes.forEach{i ->
            vertActual += vertRef[i[0]-1]
            texActual += texRef[i[1]-1]
            norActual += normalRef[i[2]-1]
        }
        return Mesh().apply {
            verts = vertActual
            normals = norActual
            textureCoords = texActual
        }
    }
}