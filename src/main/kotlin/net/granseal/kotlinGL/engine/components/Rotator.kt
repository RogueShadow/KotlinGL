package net.granseal.kotlinGL.engine.components

import com.curiouscreature.kotlin.math.Float3
import com.curiouscreature.kotlin.math.rotation

class Rotator(var angle: Float, var x: Float, var y: Float, var z: Float) : ComponentImpl() {
    constructor(angle: Float, axis: Float3) : this(angle,axis.x,axis.y,axis.z)
    override fun update(delta: Float) {
        parent.rotation *= rotation(Float3(x,y,z),angle * delta)
    }
}