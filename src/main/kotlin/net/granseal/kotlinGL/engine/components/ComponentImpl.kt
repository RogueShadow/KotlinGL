package net.granseal.kotlinGL.engine.components

import net.granseal.kotlinGL.engine.Entity

abstract class ComponentImpl : Component {
    override fun update(delta: Float) {}
    override fun draw() {}
    override fun init() {}
    override lateinit var parent: Entity
}