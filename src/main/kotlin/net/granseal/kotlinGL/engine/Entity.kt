package net.granseal.kotlinGL.engine

import net.granseal.kotlinGL.engine.math.Matrix4f
import net.granseal.kotlinGL.engine.math.Vector3f
import net.granseal.kotlinGL.engine.shaders.Shader

open class Entity{
    val components = mutableSetOf<Component>()
    val properties = mutableMapOf<String,Any>()
    var position = Vector3f()
    var scale = 1f

    fun entityMatrix(): Matrix4f {
        return Matrix4f.translate(
            position.x,
            position.y,
            position.z
        ) * rotationMatrix * Matrix4f.scale(scale, scale, scale)
    }

    private var rotationMatrix = Matrix4f.rotate(0f, 0f, 0f, 1f)

    fun rotate(angle: Float, x: Float, y: Float, z: Float) {
        rotationMatrix *= Matrix4f.rotate(angle, x, y, z)
    }
    fun rotation(angle: Float, x: Float, y: Float, z: Float){
        rotationMatrix = Matrix4f.rotate(angle, x, y, z)
    }
    fun move(x: Float, y: Float, z: Float){
        position.x += x
        position.y += y
        position.z += z
    }
    fun position(x: Float, y: Float, z: Float){
        position.x = x
        position.y = y
        position.z = z
    }

//    fun draw(){
//        material?.use(entityMatrix())
//        mesh?.draw()
//    }
    fun update(delta: Float){
        components.forEach{
            it.updateCP(delta)
        }
    }

    fun draw(){
        val ds = components.singleOrNull{ it is Shader } as Shader?
        ds?.use(entityMatrix())

        components.forEach{
            it.drawCP()
        }
    }

    fun addComponent(comp: Component): Entity{
        components.add( comp )
        comp.parent = this
        return this
    }


}



interface Component{
    fun updateCP(delta: Float)
    fun drawCP()
    var parent: Entity
}