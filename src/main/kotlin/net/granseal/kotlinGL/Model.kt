package net.granseal.kotlinGL

import java.io.File

data class Model(
    var verticies: FloatArray = floatArrayOf(),
    var textureCoords: FloatArray? = null,
    var indices: IntArray = intArrayOf(),
    var normals: FloatArray? = null,
    var objectColor: Vector3f = Vector3f(1f,1f,1f),
    var textureFile: String = ""
) {

    private val FloatLength = 4
    private var texI = 0
    private var norI = 0

    fun getCombinedFloatArray(): FloatArray {
        texI = 0
        norI = 0
        val result = mutableListOf<Float>()
        for (i in 0 until verticies.size step 3){
            result += verticies.slice(i..i+2)
            if (normals != null){
                result += normals!!.slice(norI..norI+2)
                norI += 3
            }
            if (textureCoords != null){
                result += textureCoords!!.slice(texI..texI+1)
                texI += 2
            }
        }
        return result.toFloatArray()
    }

    fun getStride(): Int {
        var stride = 3
        if (normals != null)stride += 3
        if (textureCoords != null)stride += 2
        return stride * FloatLength
    }
    fun texEnabled(): Boolean = (textureCoords != null)
    fun texOffset() = if (normals == null) 3L * FloatLength.toLong() else 6L * FloatLength.toLong()
    fun normOffset() = 3L * FloatLength.toLong()

        companion object {

            fun loadObj(file: String): Model {
                val obj = File(file).readLines()
                val verts = mutableListOf<Float>()
                val tex = mutableListOf<Float>()
                val normals = mutableListOf<Float>()
                val indicies = mutableListOf<Int>()
                val texActual = mutableListOf<Float>()
                val norActual = mutableListOf<Float>()
                obj.forEach {
                    val line = it.split(" ")
                    if (line[0] == "v"){
                        verts += line[1].toFloat()
                        verts += line[2].toFloat()
                        verts += line[3].toFloat()
                    }
                    if (line[0] == "vt"){
                        tex += line[1].toFloat()
                        tex += line[2].toFloat()
                    }
                    if (line[0] == "vn"){
                        normals += line[1].toFloat()
                        normals += line[2].toFloat()
                        normals += line[3].toFloat()
                    }
                    if (line[0] == "f"){
                        var f = line[1].split("/")
                        indicies += f[0].toInt()
                        if (f.size > 1){
                            texActual += tex[f[1].toInt()]
                        }
                        if (f.size > 2){
                            norActual += normals[f[2].toInt()]
                        }
                        f = line[2].split("/")
                        indicies += f[0].toInt()
                        if (f.size > 1){
                            texActual += tex[f[1].toInt()]
                        }
                        if (f.size > 2){
                            norActual += normals[f[2].toInt()]
                        }
                        f = line[3].split("/")
                        indicies += (f[0].toInt()-1)
                        if (f.size > 1){
                            texActual += tex[f[1].toInt()-1]
                        }
                        if (f.size > 2){
                            norActual += normals[f[2].toInt()-1]
                        }
                    }

                }

                return Model(verts.toFloatArray(),texActual.toFloatArray(),indicies.toIntArray(),norActual.toFloatArray())
            }


            fun getPlane(tex: String = ""): Model {
                val plane = Model().apply {
                    verticies = floatArrayOf(
                        -0.5f, -0.5f, 0.0f,
                        0.5f, -0.5f, 0.0f,
                        0.5f, 0.5f, 0.0f,
                        -0.5f, 0.5f, 0.0f
                    )
                    normals = floatArrayOf(
                        0f,1f,0f,
                        0f,1f,0f,
                        0f,1f,0f,
                        0f,1f,0f
                    )
                    textureCoords = floatArrayOf(
                        0f, 1f,
                        1f, 1f,
                        1f, 0f,
                        0f, 0f
                    )
                    indices = intArrayOf(
                        0, 1, 2,
                        2, 3, 0
                    )
                    textureFile = tex
                }
                return plane
            }

        fun getCube(tex: String = ""): Model{
            val cube = Model().apply{
                verticies = floatArrayOf(
                    -0.5f, -0.5f, -0.5f,
                    0.5f, -0.5f, -0.5f,
                    0.5f,  0.5f, -0.5f,
                    0.5f,  0.5f, -0.5f,
                    -0.5f,  0.5f, -0.5f,
                    -0.5f, -0.5f, -0.5f,

                    -0.5f, -0.5f,  0.5f,
                    0.5f, -0.5f,  0.5f,
                    0.5f,  0.5f,  0.5f,
                    0.5f,  0.5f,  0.5f,
                    -0.5f,  0.5f,  0.5f,
                    -0.5f, -0.5f,  0.5f,

                    -0.5f,  0.5f,  0.5f,
                    -0.5f,  0.5f, -0.5f,
                    -0.5f, -0.5f, -0.5f,
                    -0.5f, -0.5f, -0.5f,
                    -0.5f, -0.5f,  0.5f,
                    -0.5f,  0.5f,  0.5f,

                    0.5f,  0.5f,  0.5f,
                    0.5f,  0.5f, -0.5f,
                    0.5f, -0.5f, -0.5f,
                    0.5f, -0.5f, -0.5f,
                    0.5f, -0.5f,  0.5f,
                    0.5f,  0.5f,  0.5f,

                    -0.5f, -0.5f, -0.5f,
                    0.5f, -0.5f, -0.5f,
                    0.5f, -0.5f,  0.5f,
                    0.5f, -0.5f,  0.5f,
                    -0.5f, -0.5f,  0.5f,
                    -0.5f, -0.5f, -0.5f,

                    -0.5f,  0.5f, -0.5f,
                    0.5f,  0.5f, -0.5f,
                    0.5f,  0.5f,  0.5f,
                    0.5f,  0.5f,  0.5f,
                    -0.5f,  0.5f,  0.5f,
                    -0.5f,  0.5f, -0.5f
                )
                textureCoords = floatArrayOf(
                    0.0f, 0.0f,
                    1.0f, 0.0f,
                    1.0f, 1.0f,
                    1.0f, 1.0f,
                    0.0f, 1.0f,
                    0.0f, 0.0f,

                    0.0f, 0.0f,
                    1.0f, 0.0f,
                    1.0f, 1.0f,
                    1.0f, 1.0f,
                    0.0f, 1.0f,
                    0.0f, 0.0f,

                    1.0f, 0.0f,
                    1.0f, 1.0f,
                    0.0f, 1.0f,
                    0.0f, 1.0f,
                    0.0f, 0.0f,
                    1.0f, 0.0f,

                    1.0f, 0.0f,
                    1.0f, 1.0f,
                    0.0f, 1.0f,
                    0.0f, 1.0f,
                    0.0f, 0.0f,
                    1.0f, 0.0f,

                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    1.0f, 0.0f,
                    1.0f, 0.0f,
                    0.0f, 0.0f,
                    0.0f, 1.0f,

                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    1.0f, 0.0f,
                    1.0f, 0.0f,
                    0.0f, 0.0f,
                    0.0f, 1.0f
                )
                normals = floatArrayOf(
                    0f,0f,-1f,
                    0f,0f,-1f,
                    0f,0f,-1f,
                    0f,0f,-1f,
                    0f,0f,-1f,
                    0f,0f,-1f,

                    0f,0f,1f,
                    0f,0f,1f,
                    0f,0f,1f,
                    0f,0f,1f,
                    0f,0f,1f,
                    0f,0f,1f,

                    -1f,0f,0f,
                    -1f,0f,0f,
                    -1f,0f,0f,
                    -1f,0f,0f,
                    -1f,0f,0f,
                    -1f,0f,0f,

                    1f,0f,0f,
                    1f,0f,0f,
                    1f,0f,0f,
                    1f,0f,0f,
                    1f,0f,0f,
                    1f,0f,0f,

                    0f,-1f,0f,
                    0f,-1f,0f,
                    0f,-1f,0f,
                    0f,-1f,0f,
                    0f,-1f,0f,
                    0f,-1f,0f,

                    0f,1f,0f,
                    0f,1f,0f,
                    0f,1f,0f,
                    0f,1f,0f,
                    0f,1f,0f,
                    0f,1f,0f

                )
                textureFile = tex
            }
            return cube
        }


    }
}
