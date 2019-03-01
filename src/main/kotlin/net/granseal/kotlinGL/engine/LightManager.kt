package net.granseal.kotlinGL.engine

import net.granseal.kotlinGL.engine.math.Vector3f
import net.granseal.kotlinGL.engine.shaders.Light
import net.granseal.kotlinGL.engine.shaders.ShaderManager

object LightManager{
    const val MAX_LIGHTS = 16

    fun addLight(pointLight: Light) {
        lights += pointLight
    }

    fun calculateLightIndex(pos: Vector3f){
        lights.sortBy{(it.position - pos).length()}
        lights.withIndex()
            .take(MAX_LIGHTS)
            .forEach{
                it.value.update(it.index)
            }
        ShaderManager.setAllInt("engine_number_of_lights",if (lights.size <= MAX_LIGHTS) lights.size else MAX_LIGHTS)
    }

    val lights = mutableListOf<Light>()
}