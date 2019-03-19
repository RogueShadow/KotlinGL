package net.granseal.kotlinGL.engine

import com.curiouscreature.kotlin.math.*
import net.granseal.kotlinGL.engine.shaders.Light
import net.granseal.kotlinGL.engine.shaders.ShaderManager
import net.granseal.kotlinGL.engine.shaders.SunLamp

object LightManager{
    const val MAX_LIGHTS = 16

    var sunLamp: SunLamp? = null
    var sunPos: Float3 = Float3()

    fun createSunLamp(): SunLamp{
        sunLamp = SunLamp()
        return sunLamp!!
    }

    fun addLight(pointLight: Light) {
        lights += pointLight
    }

    fun calculateLightIndex(pos: Float3){
        sunPos = pos
        lights.sortBy{length((it.position() - pos))}
        lights.withIndex()
            .take(MAX_LIGHTS)
            .forEach{
                it.value.update(it.index)
            }
        ShaderManager.setGlobalUniform("engine_number_of_lights",if (lights.size <= MAX_LIGHTS) lights.size else MAX_LIGHTS)
    }

    val lights = mutableListOf<Light>()
}