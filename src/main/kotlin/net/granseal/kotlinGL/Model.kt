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
                        vertRef += Vector3f(line[1].toFloat(),line[2].toFloat(),line[3].toFloat())
                    }
                    if (line[0] == "vt"){
                        texRef += Vector2f(line[1].toFloat(),line[2].toFloat())
                    }
                    if (line[0] == "vn"){
                        normalRef += Vector3f(line[1].toFloat(),line[2].toFloat(),line[3].toFloat())
                    }
                    if (line[0] == "f"){
                        val f1 = line[1].split("/")
                        val f2 = line[2].split("/")
                        val f3 = line[3].split("/")
                        iRef += f1[0].toInt()
                        vertActual += vertRef[f1[0].toInt()-1].x
                        vertActual += vertRef[f1[0].toInt()-1].y
                        vertActual += vertRef[f1[0].toInt()-1].z
                        if (f1.size > 1){
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
                        if (f2.size > 1){
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
                        if (f3.size > 1){
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
                println("Loaded Model: $file")
                println("Verticies loaded: ${vertActual.size/3}")
                println("Normals loaded: ${norActual.size/3}")
                println("Texture Coords Loaded: ${texActual.size/2}")
                return Model().apply {
                    verticies = vertActual.toFloatArray()
                    textureCoords = texActual.toFloatArray()
                    normals = norActual.toFloatArray()
                }
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

    }
}
