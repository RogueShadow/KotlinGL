package net.granseal.kotlinGL.engine

import com.curiouscreature.kotlin.math.*
import net.granseal.kotlinGL.engine.shaders.Shader

open class Entity: Groupable, Drawable, Updatable {
    override var parent: Groupable? = null
    override var children = mutableListOf<Groupable>()
    override fun addChild(g: Groupable): Groupable{
        g.parent = this
        children.add(g)
        return g
    }
    private val components = mutableSetOf<Component>()
    var position = Float3()
    var scale = 1f
    private var rotationMatrix = Mat4.identity()

    fun components():Set<Component>{
        return components.toSet()
    }

    override fun shader(): Shader? {
        return components().singleOrNull{it is Shader} as Shader?
    }
    override fun transform(): Mat4 {
        val parent = parent
        return if (parent is Drawable){
            parent.transform() * (translation(position) * rotationMatrix * scale(Float3(scale,scale,scale)))
        }else{
            (translation(position) * rotationMatrix * scale(Float3(scale,scale,scale)))
        }
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

    override fun update(delta: Float){
        components.forEach{
            it.update(delta)
        }
        children.forEach {
            if (it is Updatable)it.update(delta)
        }
    }

    override fun draw(shader: Shader?) {
        if (shader == null) {
            shader()?.use(transform())
        }else {
            shader.use(transform())
        }

        components.forEach{
            it.draw()
        }

        children.forEach {
            if (it is Drawable) it.draw(shader)
        }
    }

    fun addComponent(comp: Component): Entity{
        components.add( comp )
        comp.parent = this
        comp.init()
        return this
    }

}


interface Groupable {
    var parent: Groupable?
    var children: MutableList<Groupable>
    fun addChild(g: Groupable): Groupable // return child
}
interface Drawable {
    fun shader(): Shader?
    fun draw(shader: Shader?)
    fun transform(): Mat4
}
interface Updatable {
    fun update(delta: Float)
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