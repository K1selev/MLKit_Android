package com.example.videostreamrecognition

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import java.util.concurrent.Executors
import android.Manifest
import android.graphics.RectF
import androidx.camera.core.ExperimentalGetImage
import androidx.core.graphics.toRect


class MainActivity : AppCompatActivity() {

    private val cameraPermission = Manifest.permission.CAMERA

    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, cameraPermission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(cameraPermission), 101)
        } else {
            startCamera()
        }
    }

    private lateinit var previewView: PreviewView
    private lateinit var graphicOverlay: GraphicOverlay

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)
        graphicOverlay = findViewById(R.id.graphicOverlay)

        requestPermissions()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()
                .also {
                    it.setAnalyzer(Executors.newSingleThreadExecutor()) { image ->
                        processImage(image)
                    }
                }


            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            } catch (exc: Exception) {
                Log.e("CameraX", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    private fun processImage(image: ImageProxy) {
        val mediaImage = image.image
        if (mediaImage == null) {
            Log.e("MLKit", "Skipping frame: mediaImage is null")
            image.close()
            return
        }
        val imageInput = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)

        val objectDetector = ObjectDetection.getClient(
            ObjectDetectorOptions.Builder()
                .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
                .enableClassification()
                .build()
        )

        objectDetector.process(imageInput)
            .addOnSuccessListener { detectedObjects ->
                graphicOverlay.clear()
                val scaleX = graphicOverlay.width.toFloat() / imageInput.width
                val scaleY = graphicOverlay.height.toFloat() / imageInput.height

                for (detectedObject in detectedObjects) {
                    val boundingBox = detectedObject.boundingBox
                    val trackingId = detectedObject.trackingId

                    val left = boundingBox.left * scaleX
                    val top = boundingBox.top * scaleY
                    val right = boundingBox.right * scaleX
                    val bottom = boundingBox.bottom * scaleY

                    val scaledBox = RectF(left, top, right, bottom).toRect()

                    graphicOverlay.add(RectOverlay(graphicOverlay, scaledBox, trackingId))
                }

                graphicOverlay.postInvalidate()
            }
            .addOnFailureListener { e ->
                Log.e("MLKit", "Object detection failed", e)
            }
            .addOnCompleteListener {
                image.close()
            }
    }

}