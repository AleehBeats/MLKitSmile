package com.example.mlkitsmile

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions


interface SmileListener {
    fun getSmileResult(result: String)
}

class ImageAnalyzer(private val smileListener: SmileListener) : ImageAnalysis.Analyzer {

    companion object {
        private const val TAG = "image_analyzer"
    }

    // Real-time contour detection
    private val realTimeOpts = FaceDetectorOptions.Builder()
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .enableTracking()
        .build()


    private val detector = FaceDetection.getClient(realTimeOpts)


    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            findFace(image)
        }
        imageProxy.close()
    }

    private fun findFace(image: InputImage) {
        detector.process(image)
            .addOnSuccessListener { list ->
                processFaces(list)
            }.addOnFailureListener {
                it.printStackTrace()
                Log.d(TAG, it.localizedMessage.toString())
            }
    }

    private fun processFaces(faces: List<Face>) {
        if (faces.isNullOrEmpty()) {
            smileListener.getSmileResult(Constants.IS_THERE_FACE)
        } else {
            for (face in faces) {
                if (face.smilingProbability != null) {
                    val smileProb = face.smilingProbability
                    if (smileProb > 0.5) {
                        smileListener.getSmileResult(Constants.SMILING_FACE)
                    } else {
                        smileListener.getSmileResult(Constants.NOT_SMILING_FACE)
                    }
                }
                else {
                    smileListener.getSmileResult(Constants.IS_THERE_FACE)
                }
            }
        }
    }
}
