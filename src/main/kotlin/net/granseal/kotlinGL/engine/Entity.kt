package net.granseal.kotlinGL.engine

import net.granseal.kotlinGL.engine.math.Matrix4f
import net.granseal.kotlinGL.engine.math.Vector3f
import net.granseal.kotlinGL.engine.shaders.DefaultShader
import net.granseal.kotlinGL.engine.shaders.Material

open class Entity(var mesh: BaseMesh? = null, var material: Material? = null){
    var position = Vector3f()
    var scale = 1f

    fun entityMatrix(): Matrix4f {
        return Matrix4f.translate(
            position.x,
            position.y,
            position.z
        ) * rotationMatrix * Matrix4f.scale(scale, scale, scale)
    }

    private var rotationMatrix = Matrix4f.rotate(0f, 0f, 0f, 1f)

    fun rotate(angle: Float, x: Float, y: Float, z: Float) {
        rotationMatrix *= Matrix4f.rotate(angle, x, y, z)
    }
    fun rotation(angle: Float, x: Float, y: Float, z: Float){
        rotationMatrix = Matrix4f.rotate(angle, x, y, z)
    }
    fun move(x: Float, y: Float, z: Float){
        position.x += x
        position.y += y
        position.z += z
    }
    fun position(x: Float, y: Float, z: Float){
        position.x = x
        position.y = y
        position.z = z
    }

    fun draw(){
        material?.use(entityMatrix())
        mesh?.draw()
    }
}