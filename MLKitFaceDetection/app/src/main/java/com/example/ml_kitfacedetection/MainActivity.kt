package com.example.ml_kitfacedetection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.IOException

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        // загружаем картинку как bitmap
        val img: ImageView = findViewById(R.id.imageFace)
        val fileName = "face2.jpg"

        val bitmap: Bitmap? = assetsToBitmap(fileName)
        bitmap?.apply {
            img.setImageBitmap(this)
        }

        // Вызывает код, занимающийся распознаванием лица
        // Задаем производительность - быстрый режим (легко производительно для моб. устройств
        // detector - непосредственно занимается распознаванием
        // image - получаем из битмапа
        // result - результат работы детектора
        val btn: Button = findViewById(R.id.btnTest)
        btn.setOnClickListener {
            val highAccuracyOpts = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .build()

            val detector = FaceDetection.getClient(highAccuracyOpts)
            val image = InputImage.fromBitmap(bitmap!!, 0)
            var result = detector.process(image)
                .addOnSuccessListener {
                faces -> //successfully
                bitmap?.apply { img.setImageBitmap(drawWithRectangle(faces)) }
            }
            .addOnFailureListener { e -> //failure

            }
        }

    }

    fun Context.assetsToBitmap(fileName: String): Bitmap? {
        return try {
            with(assets.open(fileName)) {
                BitmapFactory.decodeStream(this)
            }
        } catch(e: IOException) { null }
    }

    fun Bitmap.drawWithRectangle(faces: List<Face>): Bitmap? {
        val bitmap = copy(config, true)
        val canvas = Canvas(bitmap)
        for (face in faces) {
            val bounds = face.boundingBox
            Paint().apply {
                color = Color.RED
                style = Paint.Style.STROKE
                strokeWidth = 4.0f
                isAntiAlias = true
                canvas.drawRect(bounds, this)
            }
        }
        return bitmap
    }
}