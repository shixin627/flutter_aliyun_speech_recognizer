package com.example.flutter_aliyun_speech_recognizer

import android.util.Log
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** FlutterAliyunSpeechRecognizerPlugin */
class FlutterAliyunSpeechRecognizerPlugin : FlutterPlugin, ActivityAware, MethodCallHandler {
    private lateinit var channel: MethodChannel
    private lateinit var speechRecognizer: SpeechRecognizerModule
    private val debugTag = "AliyunSpeechRecognizer"
    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel =
            MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_aliyun_speech_recognizer")
        channel.setMethodCallHandler(this)
        speechRecognizer = SpeechRecognizerModule(recognizerCallback)
        speechRecognizer.doInit(flutterPluginBinding.applicationContext)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "start" -> {
                Log.d(debugTag, "start")
                startRecognition()
            }

            "stop" -> {
                Log.d(debugTag, "stop")
                stopRecognition()
            }

            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    private fun startRecognition() {
        checkAudioPermission()
        speechRecognizer.startRecognition()
    }

    private fun stopRecognition() {
        speechRecognizer.stopRecognition()
    }

    private var recognizerCallback: SpeechRecognizerCallback = object : SpeechRecognizerCallback {
        override fun setRecognizingState(state: Boolean) {
            // Handle the recognizing state change
            channel.invokeMethod("setRecognizingState", state)
        }

        override fun setRecognizedText(text: String) {
            // Handle the recognized text
            channel.invokeMethod("setRecognizedText", text)
        }
    }

    private fun checkAudioPermission() {
        //TODO: check if permission is granted
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        speechRecognizer.checkPermission(binding.activity, binding.activity.applicationContext)
//        speechRecognizer.doInit(binding.activity.applicationContext)
    }

    override fun onDetachedFromActivityForConfigChanges() {

    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {

    }

    override fun onDetachedFromActivity() {
        speechRecognizer.releaseNuiInstance()
    }
}
