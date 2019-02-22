/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
package net.granseal.kotlinGL

import org.lwjgl.glfw.*
import org.lwjgl.opengl.*
import org.lwjgl.system.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.system.MemoryUtil.*
import org.lwjgl.opengl.GL33.*
import kotlin.properties.Delegates
import kotlin.random.Random

abstract class KotlinGL(var width: Int, var height: Int, var TITLE:  String) {

    private var errorCallback: GLFWErrorCallback by Delegates.notNull()
    private var keyCallback: GLFWKeyCallback by Delegates.notNull()
    private var wsCallback: GLFWWindowSizeCallback by Delegates.notNull()
    private var mouseCallback: GLFWCursorPosCallback by Delegates.notNull()
    private var scrollCallback: GLFWScrollCallback by Delegates.notNull()

    private var debugProc: Callback? = null

    // The window handle
    private var window: Long = 0

    private var mousex = 0f
    private var mousey = 0f
    private var lastx = 0f
    private var lasty = 0f
    private var firstMouse = true


    // Set Title
    fun setTitle(title: String){
        glfwSetWindowTitle(window,title)
    }

    private val timer = Timer()
    fun getFPS(): Int = timer.getFPS()
    fun getTimePassed() = timer.time
    fun keyPressed(key: Int): Boolean {
        return (glfwGetKey(window,key) == 1)
    }
    fun keyReleased(key: Int): Boolean {
        return (glfwGetKey(window,key) == 3)
    }
    fun keyHeld(key: Int): Boolean {
        return (glfwGetKey(window,key) == 2)
    }

    private var rand = Random(System.nanoTime())

    fun run() {
        try {
            initGL()
            loop()

            glfwDestroyWindow(window)
            keyCallback.free()
            wsCallback.free()
            if (debugProc != null)
                debugProc!!.free()
        } finally {
            glfwTerminate()
            errorCallback.free()
        }
    }

    private fun initGL() {
        errorCallback = GLFWErrorCallback.createPrint(System.err)
        glfwSetErrorCallback(errorCallback)

        if (!glfwInit())
            throw IllegalStateException("Unable to initialize GLFW")

        glfwDefaultWindowHints() // optional, the current window hints are already the default
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3) // target version 3.3
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE)
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE) // the window will be resizable
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE) // use the core profile

        val WIDTH = width
        val HEIGHT = height

        window = glfwCreateWindow(WIDTH, HEIGHT, TITLE, NULL, NULL)
        if (window == NULL)
            throw RuntimeException("Failed to create the GLFW window")

        glfwMakeContextCurrent(window)
        glfwShowWindow(window)
        keyCallback = object: GLFWKeyCallback() {
            override fun invoke(window: Long, key: Int, scancode: Int, action: Int, mods: Int){
                if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS){
                    glfwSetWindowShouldClose(window, true)
                }else{
                    val strKey = glfwGetKeyName(key,scancode)
                    if (strKey != null)keyEvent(strKey, action)
                }
            }
        }
        glfwSetKeyCallback(window, keyCallback)
        mouseCallback = object: GLFWCursorPosCallback(){

            override fun invoke(window: Long, xpos: Double, ypos: Double) {
                mousex = xpos.toFloat()
                mousey = ypos.toFloat()
            }
        }
        scrollCallback = object: GLFWScrollCallback(){
            override fun invoke(window: Long, xoffset: Double, yoffset: Double) {
                mouseScrolled(yoffset.toFloat())
            }
        }
        glfwSetScrollCallback(window,scrollCallback)
        glfwSetCursorPosCallback(window, mouseCallback)

        wsCallback = object : GLFWWindowSizeCallback() {
            override fun invoke(window: Long, w: Int, h: Int) {
                if (w > 0 && h > 0) {
                    width = w
                    height = h
                }
            }
        }
        wsCallback.invoke(window, WIDTH, HEIGHT)
        glfwSetWindowSizeCallback(window, wsCallback)

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
        val vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor())
        glfwSetWindowPos(window, ((vidmode!!.width()) / 2) - (width/2), (vidmode.height() /2) - (height/2))
        glfwSwapInterval(0)
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
    }

    abstract fun keyEvent(key: String, action: Int)
    abstract fun initialize()
    abstract fun update(delta: Float, deltax: Float, deltay: Float)
    abstract fun draw()
    abstract fun mouseMoved(mouseX: Float, mouseY: Float, deltaX: Float, deltaY: Float)
    abstract fun mouseScrolled(delta: Float)

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
            if (firstMouse){
                lastx = mousex
                lasty = mousey
                firstMouse = false
            }
            update(timer.delta, lastx - mousex,lasty - mousey)
            lastx = mousex
            lasty = mousey
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer
            draw()
            timer.updateFPS()
            glfwSwapBuffers(window) // swap the color buffers
        }
    }
}
