package net.granseal.kotlinGL.engine

import com.curiouscreature.kotlin.math.*
import net.granseal.kotlinGL.engine.components.Component
import net.granseal.kotlinGL.engine.shaders.Shader


open class Entity {
    //config vars
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

    fun update(delta: Float) {
        components.forEach {
            it.update(delta)
        }
        children.forEach {
            it.update(delta)
        }
    }

    fun draw(shader: Shader?) {
        if (shader == null) {
            shader()?.use(transform() * scale())
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
        components.add(comp)
        comp.parent = this
        comp.init()
        return this
    }

    private fun shader(): Shader? {
        return components().singleOrNull { it is Shader } as Shader?
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
