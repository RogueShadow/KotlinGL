package net.granseal.kotlinGL.engine.components

import net.granseal.kotlinGL.engine.Entity

interface Component {
    fun update(delta: Float)
    fun draw()
    fun init()
    var parent: Entity
}
