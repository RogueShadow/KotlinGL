package net.granseal.kotlinGL.engine

import com.curiouscreature.kotlin.math.*
import net.granseal.kotlinGL.engine.components.Component
import net.granseal.kotlinGL.engine.components.ComponentImpl
import net.granseal.kotlinGL.engine.shaders.DefaultShader
import net.granseal.kotlinGL.engine.shaders.Shader


open class Entity {
    //config vars
    var id = ""
    var inheritScale = false

    //grouping
    var parent: Entity? = null
    var children = mutableListOf<Entity>()

    //transformation
    var scale = Float3(1f, 1f, 1f)
    var position = Float3()
    var rotation = Mat4.identity()

    //components
    private val components = mutableSetOf<Component>()

    fun components(): Set<Component> {
        return components.toSet()
    }

    fun addChild(g: Entity): Entity {
        g.parent = this
        children.add(g)
        return g
    }

    fun addChild(block: Entity.() -> Unit): Entity {
        val e = Entity()
        e.apply(block)
        e.parent = this
        children.add(e)
        return e
    }

    fun update(delta: Float) {
        components.forEach {
            it.update(delta)
        }
        val removeList = components.filter{it.remove}
        components.removeAll(removeList)
        children.forEach {
            it.update(delta)
        }
    }

    fun draw(shader: Shader?) {
        if (shader == null) {
            getComponentByType<Shader>()?.use(transform() * scale())
        } else {
            shader.use(transform() * scale())
        }

        components.forEach {
            it.draw()
        }

        children.forEach {
            it.draw(shader)
        }
    }

    fun addComponent(comp: Component): Entity {
        comp.parent = this
        comp.init()
        components.add(comp)
        return this
    }

    fun addComponent(block: () -> Component) = addComponent(block.invoke())

    inline fun <reified T> getComponentByType(): T? {
        return this.components().firstOrNull{it is T} as T?
    }

    fun getEntityById(id: String): Entity? {
        if (this.id == id){
            return this
        }else{
            children.forEach{
                val result = it.getEntityById(id)
                if (result != null) {
                    return result
                }
            }
            return null
        }
    }

    fun getAllEntityById(id: String): List<Entity> {
        val result = mutableListOf<Entity>()
        if (this.id == id)result += this
        children.forEach{
            result.addAll(it.getAllEntityById(id))
        }
        return result.toList()
    }

    private fun localTransform(): Mat4 {
        return translation(position) * rotation
    }

    private fun transform(): Mat4 {
        val parent = parent
        return if (parent != null) {
            parent.transform() * localTransform()
        } else {
            localTransform()
        }
    }

    private fun scale(): Mat4 {
        return if (inheritScale) {
            val parent = parent
            val pScale = parent?.scale()
            if (pScale != null) pScale * scale(scale) else scale(scale)
        } else {
            scale(scale)
        }
    }
}
