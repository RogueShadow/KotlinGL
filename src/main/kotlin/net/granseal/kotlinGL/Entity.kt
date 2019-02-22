package net.granseal.kotlinGL

class Entity(val vao: VertexArrayObject){
    fun modelMatrix() =  translationMatrix * rotationMatrix * scaleMatrix

    private var scaleMatrix = Matrix4f.scale(1f,1f,1f)
    private var rotationMatrix = Matrix4f.rotate(0f,0f,0f,1f)
    private var translationMatrix = Matrix4f.translate(0f,0f,0f)

    fun rotate(angle: Float, x: Float, y: Float, z: Float) {
        rotationMatrix *= Matrix4f.rotate(angle,x,y,z)
    }
    fun rotation(angle: Float, x: Float, y: Float, z: Float){
        rotationMatrix = Matrix4f.rotate(angle,x,y,z)
    }
    fun move(x: Float, y: Float, z: Float){
        translationMatrix *= Matrix4f.translate(x,y,z)
    }
    fun position(x: Float, y: Float, z: Float){
        translationMatrix = Matrix4f.translate(x,y,z)
    }
    fun scale(x: Float, y: Float, z: Float){
        scaleMatrix = Matrix4f.scale(x,y,z)
    }

    fun draw(){
        vao.shader.setUniformMat4("transform",modelMatrix())
        vao.draw()
    }
}