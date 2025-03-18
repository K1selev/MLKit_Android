package com.example.mlkit_digital_ink_recognition

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.vision.digitalink.DigitalInkRecognition
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer
import com.google.mlkit.vision.digitalink.DigitalInkRecognizerOptions
import com.google.mlkit.vision.digitalink.Ink

import com.google.mlkit.common.model.RemoteModelManager



class MainActivity : AppCompatActivity() {

    private lateinit var drawingView: DrawingView
    private lateinit var txtOutput: TextView
    private lateinit var btnRecognize: Button
    private lateinit var btnClear: Button

    private var model: DigitalInkRecognitionModel? = null
    private var recognizer: DigitalInkRecognizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация UI
        drawingView = findViewById(R.id.drawingView)
        txtOutput = findViewById(R.id.txtOutput)
        btnRecognize = findViewById(R.id.btnRecognize)
        btnClear = findViewById(R.id.btnClear)

        // Инициализация распознавания
        initializeRecognition()

        // Обработчики для кнопок
        btnRecognize.setOnClickListener {
            recognizeText()
        }

        btnClear.setOnClickListener {
            clearDrawing()
        }
    }

    // Строим модель распознавания и загружаем ее
    private fun initializeRecognition() {
        val modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag("en-US")
        if (modelIdentifier != null) {
            model = DigitalInkRecognitionModel.builder(modelIdentifier).build()
            val remoteModelManager = RemoteModelManager.getInstance()

            remoteModelManager.download(model!!, DownloadConditions.Builder().build())
                .addOnSuccessListener {
                    Log.i("DigitalInk", "Model downloaded successfully")
                    recognizer = DigitalInkRecognition.getClient(
                        DigitalInkRecognizerOptions.builder(model!!).build()
                    )
                }
                .addOnFailureListener { e: Exception ->
                    Log.e("DigitalInk", "Error downloading model", e)
                }
        }
    }

    // Метод распознавния текста с рисунка
    // Получаем Ink (нарисвованные данные)
    // Проходим по кандидатам и выводим распознанный текст
    private fun recognizeText() {
        val ink = drawingView.getInk()
        recognizer?.recognize(ink)
            ?.addOnSuccessListener { result ->
                var outputString = ""
                for (candidate in result.candidates) {
                    outputString += candidate.text + "\n"
                }
                txtOutput.text = outputString
            }
            ?.addOnFailureListener { e ->
                Log.e("DigitalInk", "Recognition failed", e)
            }
    }

    // Метод для очистки экрана текста
    private fun clearDrawing() {
        drawingView.clear()
        txtOutput.text = ""
    }
}

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val path = Path()
    private val paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }
    private var inkBuilder = Ink.builder()
    private var strokeBuilder: Ink.Stroke.Builder? = null

    // Метод рисования на экране
    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }

    // Метод для обработки событий касания
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // начало линии
                path.moveTo(x, y)
                strokeBuilder = Ink.Stroke.builder()
                strokeBuilder?.addPoint(Ink.Point.create(x, y, System.currentTimeMillis()))
            }
            MotionEvent.ACTION_MOVE -> {
                // рисуем
                path.lineTo(x, y)
                strokeBuilder?.addPoint(Ink.Point.create(x, y, System.currentTimeMillis()))
            }
            MotionEvent.ACTION_UP -> {
                // завершаем рисовние
                strokeBuilder?.addPoint(Ink.Point.create(x, y, System.currentTimeMillis()))
                strokeBuilder?.build()?.let { inkBuilder.addStroke(it) }
                strokeBuilder = null
            }
        }
        invalidate()
        return true
    }

    // метод для получения данных Ink
    fun getInk(): Ink {
        return inkBuilder.build()
    }

    // Метод для очистки экрана и сброса Ink
    fun clear() {
        path.reset()
        inkBuilder = Ink.builder()
        invalidate()
    }
}