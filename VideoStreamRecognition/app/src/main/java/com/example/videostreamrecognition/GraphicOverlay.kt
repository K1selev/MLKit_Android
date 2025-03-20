package com.example.videostreamrecognition

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View

class GraphicOverlay(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val lock = Any()
    private val graphics = mutableListOf<Graphic>()

    abstract class Graphic(protected val overlay: GraphicOverlay) {
        abstract fun draw(canvas: Canvas)
    }

    fun clear() {
        synchronized(lock) {
            graphics.clear()
        }
        postInvalidate()
    }

    fun add(graphic: Graphic) {
        synchronized(lock) {
            graphics.add(graphic)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        synchronized(lock) {
            graphics.forEach { it.draw(canvas) }
        }
    }
}
