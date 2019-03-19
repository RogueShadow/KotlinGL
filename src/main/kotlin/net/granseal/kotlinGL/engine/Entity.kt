package net.granseal.kotlinGL.engine

import com.curiouscreature.kotlin.math.*
import net.granseal.kotlinGL.engine.shaders.Shader

open class Entity{
    private val components = mutableSetOf<Component>()
    var position = Float3()
    var scale = 1f
    private var rotationMatrix = Mat4.identity()

    fun components():Set<Component>{
        return components.toSet()
    }
    fun entityMatrix(): Mat4 {
        return translation(position) * rotationMatrix * scale(Float3(scale, scale, scale))
    }


    fun rotate(angle: Float, x: Float, y: Float, z: Float) {
        rotationMatrix *= rotation(Float3(x, y, z), angle)
    }
    fun rotation(angle: Float, x: Float, y: Float, z: Float){
        rotationMatrix = rotation(Float3(x,y,z),angle)
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

    fun draw(shader: Shader? = components.singleOrNull{it is Shader} as Shader?){
        if (shader == null)return
        shader.use(entityMatrix())

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