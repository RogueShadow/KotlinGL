package net.granseal.kotlinGL.engine

import net.granseal.kotlinGL.engine.math.Vector3f
import net.granseal.kotlinGL.engine.shaders.Light
import net.granseal.kotlinGL.engine.shaders.ShaderManager
import net.granseal.kotlinGL.engine.shaders.SunLamp

object LightManager{
    const val MAX_LIGHTS = 16

    var sunLamp: SunLamp? = null
    var sunPos: Vector3f = Vector3f()

    fun createSunLamp(): SunLamp{
        sunLamp = SunLamp()
        return sunLamp!!
    }

    fun addLight(pointLight: Light) {
        lights += pointLight
    }

    fun calculateLightIndex(pos: Vector3f){
        sunPos = pos
        lights.sortBy{(it.position() - pos).length()}
        lights.withIndex()
            .take(MAX_LIGHTS)
            .forEach{
                it.value.update(it.index)
            }
        ShaderManager.setAllInt("engine_number_of_lights",if (lights.size <= MAX_LIGHTS) lights.size else MAX_LIGHTS)
    }

    val lights = mutableListOf<Light>()
}