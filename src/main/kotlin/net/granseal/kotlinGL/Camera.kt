package net.granseal.kotlinGL

import kotlin.math.cos
import kotlin.math.sin

class Camera {
    private val pitchMax = (Math.PI.toFloat()/2f)*.9f
    private var pos: Vector3f = Vector3f()
    private var up = Vector3f(0f,1f,0f)
    private var front = Vector3f(0f,0f,1f)
    var pitch = 0f
    var yaw = 0f
    var view = Matrix4f()
    var projection = Matrix4f()

    fun setPerspective(fov: Float, aspect: Float, near: Float, far: Float){
        projection = Matrix4f.perspective(fov,aspect,near,far)
    }

    fun move(deltaX: Float, deltaY: Float, deltaZ: Float){
        pos.x += deltaX
        pos.y += deltaY
        pos.z += deltaZ
        cameraLook()
    }
    fun position(x: Float, y: Float, z: Float){
        pos = Vector3f(x,y,z)
    }
    fun updateCamera(deltaX: Float, deltaY: Float){
        yaw -= deltaX
        pitch += deltaY

        if (pitch > pitchMax)pitch = pitchMax

        if (pitch < -pitchMax)pitch = -pitchMax

        front.x = cos(pitch) * cos(yaw)
        front.y = sin(pitch)
        front.z = cos(pitch) * sin(yaw)

        front.normalize()

        cameraLook()
    }

    fun cameraLook() = lookAt(pos + front)

    fun lookAt(target: Vector3f) {
        val d = (pos - target).normalize()
        val r = (up cross d ).normalize()
        val u = d cross r
        val viewMatrix = Matrix4f(
            Vector4f(r.x,r.y,r.z,0f),
            Vector4f(u.x,u.y,u.z,0f),
            Vector4f(d.x,d.y,d.z,0f),
            Vector4f(0f,0f,0f,1f)).transpose()
        val posMat = Matrix4f.translate(-pos.x,-pos.y,-pos.z)
        front = d.negate()

        view = viewMatrix * posMat
    }

    fun forward(amount: Float){
        pos += front scale amount
        cameraLook()
    }
    fun backword(amount: Float){
        pos -= front scale amount
        cameraLook()
    }
    fun right(amount: Float){
        pos -= (front cross up).normalize() scale amount
        cameraLook()
    }
    fun left(amount: Float){
        pos += (front cross up).normalize() scale amount
        cameraLook()
    }
    fun up(amount: Float){
        val r = (up cross front).normalize()
        val u = (front cross r).normalize()
        pos += u scale amount
    }
    fun down(amount: Float){
        val r = (up cross front).normalize()
        val u = (front cross r).normalize()
        pos -= u scale amount
    }
}