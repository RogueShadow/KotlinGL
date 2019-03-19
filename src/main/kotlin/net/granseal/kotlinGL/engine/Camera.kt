package net.granseal.kotlinGL.engine

import com.curiouscreature.kotlin.math.*
import kotlin.math.cos
import kotlin.math.sin

class Camera {
    var pos = Float3(0f,0f,0f)
    var front = Float3(0f, 0f, -1f)
    var up = Float3(0f, 1f, 0f)
    var right = Float3(0f,0f,0f)
    var worldUp = Float3(0f, 1f, 0f)
    var yaw = 0.0
    var pitch = 0.0
    var cameraSpeed = 6f
    var sensativity = 40f
    private val pitchMax = 89.0
    var view = Mat4()
    var projection = Mat4()

    fun updateCamera(deltaX: Float, deltaY: Float, delta: Float){
        yaw -= deltaX*delta*sensativity
        pitch +=  deltaY*delta*sensativity

        if (pitch > pitchMax)pitch = pitchMax
        if (pitch < -pitchMax)pitch = -pitchMax

        front.x = cos(radians(yaw.toFloat())) * cos(radians(pitch.toFloat()))
        front.y = sin(radians(pitch.toFloat()))
        front.z = sin(radians(yaw.toFloat())) * cos(radians(pitch.toFloat()))
        front = normalize(front)

        updateCameraVectors()
    }

    fun updateCameraVectors(){

        right = normalize(front x worldUp)
        up    = right x front

        view = lookAt(pos,pos+front,up)
        view = inverse(view) //Why must I do this?!
    }

    fun forward(amount: Float){
        pos += (amount * cameraSpeed) * front
    }
    fun backword(amount: Float){
        pos -= (amount * cameraSpeed) * front
    }
    fun right(amount: Float){
        pos -= right * (amount * cameraSpeed)
        
    }
    fun left(amount: Float){
        pos += right * (amount * cameraSpeed)
        
    }
    fun up(amount: Float){
        pos += up * (amount * cameraSpeed)
        
    }
    fun down(amount: Float){
        pos -= up * (amount * cameraSpeed)
    }
}