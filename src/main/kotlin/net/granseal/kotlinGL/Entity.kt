package net.granseal.kotlinGL

class Entity(val vao: VertexArrayObject){
    var position = Vector3f()
    var scale = 1f

    fun modelMatrix(): Matrix4f {
        return Matrix4f.translate(position.x, position.y, position.z) * rotationMatrix * Matrix4f.scale(scale, scale, scale)
    }

    private var rotationMatrix = Matrix4f.rotate(0f,0f,0f,1f)

    fun rotate(angle: Float, x: Float, y: Float, z: Float) {
        rotationMatrix *= Matrix4f.rotate(angle,x,y,z)
    }
    fun rotation(angle: Float, x: Float, y: Float, z: Float){
        rotationMatrix = Matrix4f.rotate(angle,x,y,z)
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
        vao.shader.setUniformMat4("transform",modelMatrix())
        vao.draw()
    }
}