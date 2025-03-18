package com.example.mlkit_imagemodule

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.io.IOException

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
        // название файла
        val fileName = "cat.jpg" // bird  cat  dog  mouse
        // получаем битовую карту
        val bitmap:Bitmap? = assetsToBitmap(fileName)
        bitmap?.apply {
            img.setImageBitmap(this)
        }
        val txtOutput: TextView = findViewById(R.id.txtOutput)
        val btn: Button = findViewById(R.id.btnTest)

        // Когда пользователь нажимает на кнопку, код создаст этикетку изображения из ML Kit с
        // параметрами по умолчанию
        // После этого он создаст объект изображения из битовой карты
        // Для обработки изображения будет вызван маркировщик, к которому добавлены два слушателя:
        // успех и неудача
        btn.setOnClickListener {
            val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
            val image = InputImage.fromBitmap(bitmap!!, 0)
            var outputText = ""
            labeler.process(image).addOnSuccessListener { labels ->
                for (label in labels) {
                    val text = label.text
                    val confidence = label.confidence
                    outputText += "$text: $confidence\n"
                }
                txtOutput.text = outputText
            }
                .addOnFailureListener { e -> }
        }

        //val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
    }

    // Позволяет загрузить изображения из папки assets как битовые карты
    fun Context.assetsToBitmap(fileName: String): Bitmap? {
        return try {
            with(assets.open(fileName)) {
                BitmapFactory.decodeStream(this)
            }
        }
        catch(e: IOException) { null }
    }
}