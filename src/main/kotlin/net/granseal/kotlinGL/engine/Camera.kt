package net.granseal.kotlinGL.engine

import net.granseal.kotlinGL.engine.math.Matrix4f
import net.granseal.kotlinGL.engine.math.Vector3f
import net.granseal.kotlinGL.engine.math.Vector4f
import kotlin.math.cos
import kotlin.math.sin

class Camera {
    private val pitchMax = 89.0

    var pos: Vector3f = Vector3f()
    private set

    private var up = Vector3f(0f, 1f, 0f)
    private var front = Vector3f(0f, 0f, 1f)
    private var moveDelta = Vector3f()
    var sensativity = 40f
    var cameraSpeed = 6f
    var pitch = 0.0
    var yaw = 0.0
    var view = Matrix4f()
    var projection = Matrix4f.perspective(45f,1920f/1080f,0.1f,100f)

    fun setPerspective(fov: Float, aspect: Float, near: Float, far: Float){
        projection = Matrix4f.perspective(fov, aspect, near, far)
    }

    fun move(deltaX: Float, deltaY: Float, deltaZ: Float){
        pos.x += deltaX
        pos.y += deltaY
        pos.z += deltaZ
    }
    fun position(x: Float, y: Float, z: Float){
        pos = Vector3f(x, y, z)
    }
    fun updateCamera(deltaX: Float, deltaY: Float, delta: Float){
        yaw -= deltaX*delta*sensativity
        pitch +=  deltaY*delta*sensativity

        if (pitch > pitchMax)pitch = pitchMax

        if (pitch < -pitchMax)pitch = -pitchMax

        front.x = cos(Math.toRadians(pitch).toFloat()) * cos(Math.toRadians(yaw).toFloat())
        front.y = sin(Math.toRadians(pitch).toFloat())
        front.z = cos(Math.toRadians(pitch).toFloat()) * sin(Math.toRadians(yaw).toFloat())

        front.normalize()

        pos += (moveDelta - pos).normalize().scale(delta*cameraSpeed)

        cameraLook()

        moveDelta.apply {
            x = pos.x
            y = pos.y
            z = pos.z
        }
    }

    fun cameraLook() = lookAt(pos + front)

    fun lookAt(target: Vector3f) {
        val d = (pos - target).normalize()
        val r = (up cross d).normalize()
        val u = d cross r
        val viewMatrix = Matrix4f(
            Vector4f(r.x, r.y, r.z, 0f),
            Vector4f(u.x, u.y, u.z, 0f),
            Vector4f(d.x, d.y, d.z, 0f),
            Vector4f(0f, 0f, 0f, 1f)
        ).transpose()
        val posMat = Matrix4f.translate(-pos.x, -pos.y, -pos.z)
        front = d.negate()
        view = viewMatrix * posMat
    }

    fun forward(amount: Float){
        moveDelta += front scale (amount * cameraSpeed)

    }
    fun backword(amount: Float){
        moveDelta -= front scale (amount * cameraSpeed)
        
    }
    fun right(amount: Float){
        moveDelta -= (front cross up).normalize() scale (amount * cameraSpeed)
        
    }
    fun left(amount: Float){
        moveDelta += (front cross up).normalize() scale (amount * cameraSpeed)
        
    }
    fun up(amount: Float){
        val r = (up cross front).normalize()
        val u = (front cross r).normalize()
        moveDelta += u scale (amount * cameraSpeed)
        
    }
    fun down(amount: Float){
        val r = (up cross front).normalize()
        val u = (front cross r).normalize()
        moveDelta -= u scale (amount * cameraSpeed)
    }
}