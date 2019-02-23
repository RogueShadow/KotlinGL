/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
package net.granseal.kotlinGL.engine

import org.lwjgl.glfw.*
import org.lwjgl.opengl.*
import org.lwjgl.system.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.system.MemoryUtil.*
import org.lwjgl.opengl.GL33.*
import kotlin.properties.Delegates
import kotlin.random.Random

abstract class KotlinGL(var width: Int, var height: Int, var TITLE:  String) {

    private var debugProc: Callback? = null

    // The window handle
    private var window: Long = 0

    private var mousex = 0f
    private var mousey = 0f
    private var lastx = 0f
    private var lasty = 0f


    var mouseGrabbed = true
        set(value){
            glfwSetInputMode(window, GLFW_CURSOR,if (value) GLFW_CURSOR_DISABLED else GLFW_CURSOR_NORMAL)
            field = value
        }


    private val timer = Timer()
    private var rand = Random(System.nanoTime())

    fun run() {
        try {
            initGL()
            loop()
            glfwDestroyWindow(window)
            if (debugProc != null)
                debugProc!!.free()
        } finally {
            glfwTerminate()
        }
    }


    private fun initGL() {
        //Get our window from GLFW
        glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err))

        if (!glfwInit())
            throw IllegalStateException("Unable to initialize GLFW")

        glfwDefaultWindowHints() // optional, the current window hints are already the default
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3) // target version 3.3
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE)
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE) // the window will be resizable
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE) // use the core profile

        window = glfwCreateWindow(width, height, TITLE, NULL, NULL)
        if (window == NULL)
            throw RuntimeException("Failed to create the GLFW window")

        glfwMakeContextCurrent(window)
        glfwShowWindow(window)

        //Create all the callbacks.
        glfwSetKeyCallback(window,object: GLFWKeyCallback() {
            override fun invoke(window: Long, key: Int, scancode: Int, action: Int, mods: Int){
                if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS){
                    glfwSetWindowShouldClose(window, true)
                }else{
                    val strKey = glfwGetKeyName(key,scancode)
                    if (strKey != null)keyEvent(strKey, action)
                }
            }
        })
        glfwSetMouseButtonCallback(window,object: GLFWMouseButtonCallback(){
            override fun invoke(window: Long, button: Int, action: Int, mods: Int) {
                mouseClicked(button,action,mousex, mousey)
            }

        })
        glfwSetCursorEnterCallback(window,object: GLFWCursorEnterCallback(){
            override fun invoke(window: Long, entered: Boolean) {
            }

        })
        glfwSetCursorPosCallback(window,object: GLFWCursorPosCallback(){

            override fun invoke(window: Long, xpos: Double, ypos: Double) {
                mousex = xpos.toFloat()
                mousey = ypos.toFloat()
            }
        })
        glfwSetScrollCallback(window,object: GLFWScrollCallback(){
            override fun invoke(window: Long, xoffset: Double, yoffset: Double) {
                mouseScrolled(yoffset.toFloat())
            }
        })
        glfwSetWindowSizeCallback(window,object : GLFWWindowSizeCallback() {
            override fun invoke(window: Long, w: Int, h: Int) {
                if (w > 0 && h > 0) {
                    width = w
                    height = h
                }
            }
        })
        glfwSetFramebufferSizeCallback(window, object: GLFWFramebufferSizeCallback() {
            override fun invoke(window: Long, width: Int, height: Int) {
                glViewport(0,0,width,height)
            }
        })
        glfwSetCharCallback(window, object: GLFWCharCallback(){
            override fun invoke(window: Long, codepoint: Int) {

            }
        })
        glfwSetJoystickCallback(object: GLFWJoystickCallback(){
            override fun invoke(jid: Int, event: Int) {
                println("JoystickEvent jid($jid) event($event)")
            }
        })

        //Center the window on the screen
        val vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor())
        glfwSetWindowPos(window, ((vidmode!!.width()) / 2) - (width/2), (vidmode.height() /2) - (height/2))

        //Disable vsync
        glfwSwapInterval(0)

        //grab mouse cursor by default, disable by setting mouseGrabbed = false
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
    }

    //Setting initial GL State.
    //Main Loop, engine code.
    private fun loop() {
        GL.createCapabilities()
        debugProc = GLUtil.setupDebugMessageCallback()
        glEnable(GL_BLEND)
        glEnable(GL11.GL_DEPTH_TEST)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        timer.init()
        initialize()
        //glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
        glClearColor(0f,0f,0f,1f)

        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents()
            timer.update()
            timer.updateUPS()
            update(timer.delta, lastx - mousex,lasty - mousey)
            lastx = mousex
            lasty = mousey
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer
            draw()
            timer.updateFPS()
            glfwSwapBuffers(window) // swap the color buffers
        }
    }

    //Events sent to engine implementation
    abstract fun mouseClicked(button: Int,action: Int, mousex: Float, mousey: Float)
    abstract fun keyEvent(key: String, action: Int)
    abstract fun initialize()
    abstract fun update(delta: Float, deltax: Float, deltay: Float)
    abstract fun draw()
    abstract fun mouseMoved(mouseX: Float, mouseY: Float, deltaX: Float, deltaY: Float)
    abstract fun mouseScrolled(delta: Float)

    //Functions to retrieve useful state information and input
    fun getFPS(): Int = timer.getFPS()
    fun getTimePassed() = timer.time
    fun keyPressed(key: Int)=(glfwGetKey(window,key) == 1)
    fun keyReleased(key: Int)=(glfwGetKey(window,key) == 3)
    fun keyHeld(key: Int)=(glfwGetKey(window,key) == 2)
    fun mouseClicked(button: Int) =  (glfwGetMouseButton(window,button) == GLFW_PRESS)
    fun mouseReleased(button: Int) = (glfwGetMouseButton(window,button) == GLFW_RELEASE)

    //Handy functions.
    fun setTitle(title: String) = glfwSetWindowTitle(window,title)
}
