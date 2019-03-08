package net.granseal.kotlinGL.engine.renderer
import net.granseal.kotlinGL.engine.Entity

interface Renderer {
    fun initialize()
    fun render(e: Entity)
    fun next(): Boolean
}