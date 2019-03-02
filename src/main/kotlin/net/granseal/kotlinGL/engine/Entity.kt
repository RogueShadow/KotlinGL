package net.granseal.kotlinGL.engine

import net.granseal.kotlinGL.engine.math.Matrix4f
import net.granseal.kotlinGL.engine.math.Vector3f
import net.granseal.kotlinGL.engine.shaders.Shader

open class Entity{
    private val components = mutableSetOf<Component>()
    var position = Vector3f()
    var scale = 1f

    fun components():Set<Component>{
        return components.toSet()
    }
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

    fun update(delta: Float){
        components.forEach{
            it.update(delta)
        }
    }

    fun draw(){
        val shader = components.singleOrNull{ it is Shader } as Shader?
        shader?.use(entityMatrix())

        components.forEach{
            it.draw()
        }
    }

    fun addComponent(comp: Component): Entity{
        components.add( comp )
        comp.parent = this
        comp.init()
        return this
    }

}



interface Component{
    fun update(delta: Float)
    fun draw()
    fun init()
    var parent: Entity
}

abstract class ComponentImpl: Component {
    override fun update(delta: Float){}
    override fun draw(){}
    override fun init(){}
    override lateinit var parent: Entity
}