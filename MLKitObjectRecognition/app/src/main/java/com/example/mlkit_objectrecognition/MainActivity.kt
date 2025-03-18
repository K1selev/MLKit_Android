package com.example.mlkit_objectrecognition

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.IOException

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        val img: ImageView = findViewById(R.id.imageToLabel)
        val fileName = "birds.jpg" // cars  birds cats  dogs  pets
        val bitmap: Bitmap? = assetsToBitMap(fileName)
        bitmap?.apply {
            img.setImageBitmap(this)
        }

        val txtOutput: TextView = findViewById(R.id.txtOutput)
        val btn: Button = findViewById(R.id.btnTest)

        // Обнаружении нескольких объектов на изображении
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .build()

        btn.setOnClickListener {
            val objectDetector = ObjectDetection.getClient(options)
            var image = InputImage.fromBitmap(bitmap!!,0)
            objectDetector.process(image)
                .addOnSuccessListener { detectedObjects ->
                    bitmap?.apply {
                        img.setImageBitmap(drawWithRectangle(detectedObjects))
                        getLabels(bitmap, detectedObjects, txtOutput)
                    }
                }
                .addOnFailureListener { e -> }
        }

    }

    fun Context.assetsToBitMap(fileName:String): Bitmap? {
        return try {
            with(assets.open(fileName)) {
                BitmapFactory.decodeStream(this)
            }
        }
        catch (e: IOException) { null }
    }

    // создает копию растрового изображения и новый холст на его основе. Затем
    // выполняется итерация по всем обнаруженным объектам.
    fun Bitmap.drawWithRectangle(objects: List<DetectedObject>): Bitmap? {
        //val bitmap = copy(config, true)
        val bitmapConfig = this.config ?: Bitmap.Config.ARGB_8888
        val bitmap = copy(bitmapConfig, true)

        val canvas = Canvas(bitmap)
        var thisLabel = 0
        for (obj in objects) {
            thisLabel++
            val bounds = obj.boundingBox
            Paint().apply {
                color = Color.RED
                style = Paint.Style.STROKE
                textSize = 32.0f
                strokeWidth = 4.0f
                isAntiAlias = true
                // рисуем
                canvas.drawRect(bounds, this)
                canvas.drawText(thisLabel.toString(),
                bounds.left.toFloat(), bounds.top.toFloat(), this)
            }
        }
        return bitmap
    }

    // Этот код перебирает каждый из обнаруженных обхектов и использует ограничивающую рамку
    // для создания нового битового изображения под названием croppedBitmap.
    // Затем он будет использовать этикетировщик изображений (labeler), настроенный с
    // параметрами по умолчанию, для обработки этого нового изображения
    // При успешном возврате у него будет несколько меток, которые он запишет в строку,
    // разделенную запятыми, которая будет выведена в txtOutput.
    fun getLabels(bitmap: Bitmap, objects: List<DetectedObject>, txtOutput: TextView) {
        val labeler = ImageLabeling.getClient(
            ImageLabelerOptions.DEFAULT_OPTIONS
        )
        for (obj in objects) {
            val bounds = obj.boundingBox
            val croppedBitmap = Bitmap.createBitmap(bitmap,
                bounds.left, bounds.top,
                bounds.width(), bounds.height())
            var image = InputImage.fromBitmap(croppedBitmap!!, 0)
            labeler.process(image)
                .addOnSuccessListener { labels ->
                    var labelText = ""
                    if(labels.count() > 0) {
                        labelText = txtOutput.text.toString()
                        for (thisLabel in labels) {
                            labelText += thisLabel.text + ","
                        }
                        labelText += "\n"
                    }
                    else { labelText = "Not found." + "\n"}
                    txtOutput.text = labelText.toString()
                }
        }
    }
}