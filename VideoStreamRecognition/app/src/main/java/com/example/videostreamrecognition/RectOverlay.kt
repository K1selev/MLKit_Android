package com.example.videostreamrecognition

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect

class RectOverlay(
    overlay: GraphicOverlay,
    private val boundingBox: Rect,
    private val trackingId: Int?
) : GraphicOverlay.Graphic(overlay) {

    private val paint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
    }

    override fun draw(canvas: Canvas) {
        canvas.drawRect(boundingBox, paint)
        trackingId?.let {
            canvas.drawText("Tracking ID: $it", boundingBox.left.toFloat(), boundingBox.top.toFloat(), textPaint)
        }
    }
}
