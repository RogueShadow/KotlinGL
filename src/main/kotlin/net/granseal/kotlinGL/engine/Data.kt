package net.granseal.kotlinGL.engine

import com.curiouscreature.kotlin.math.Float2
import com.curiouscreature.kotlin.math.Float3
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.lwjgl.opengl.GL33
import java.io.File

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
        quad.verts = mutableListOf(
            Float3(-1.0f,-1.0f,0.0f),
            Float3(-1.0f,1.0f,0.0f),
            Float3(1.0f,1.0f,0.0f),
            Float3(1.0f,-1.0f,0.0f)
        )
        quad.normals = mutableListOf(
            Float3(1f,0f,0f),
            Float3(1f,0f,0f),
            Float3(1f,0f,0f),
            Float3(1f,0f,0f)
        )
        quad.textureCoords = mutableListOf(
           Float2(1f,1f),
           Float2(1f,0f),
           Float2(0f,0f),
           Float2(0f,1f)
        )
        quad.type = GL33.GL_TRIANGLE_FAN
        meshes.putIfAbsent("quad",quad)
    }
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