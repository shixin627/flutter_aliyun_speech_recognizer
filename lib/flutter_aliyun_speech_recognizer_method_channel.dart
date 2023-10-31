import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_aliyun_speech_recognizer_platform_interface.dart';

/// An implementation of [FlutterAliyunSpeechRecognizerPlatform] that uses method channels.
class MethodChannelFlutterAliyunSpeechRecognizer extends FlutterAliyunSpeechRecognizerPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_aliyun_speech_recognizer');

  MethodChannelFlutterAliyunSpeechRecognizer() {
    methodChannel.setMethodCallHandler(_methodCallHandler);
  }

  final StreamController<bool> _recognizingStateController =
  StreamController.broadcast();

  final StreamController<String> _recognizedTextController =
  StreamController.broadcast();

  Future<dynamic> _methodCallHandler(MethodCall methodCall) {
    if ("setRecognizingState" == methodCall.method) {
      bool state = methodCall.arguments;
      debugPrint("Pass RecognizingState----$state----to Flutter");
      _recognizingStateController.add(state);
    } else if ("setRecognizedText" == methodCall.method) {
      String text = methodCall.arguments;
      debugPrint("Pass RecognizedText---$text---to Flutter");
      _recognizedTextController.add(text);
    }
    return Future.value(true);
  }


  @override
  Future<void> start(String token) async {
    await methodChannel.invokeMethod<void>('start', {'token': token});
  }

  @override
  Future<void> stop() async {
    await methodChannel.invokeMethod<void>('stop');
  }

  @override
  Stream<bool> get recognizingStateStream =>
      _recognizingStateController.stream;

  @override
  Stream<String> get recognizedTextStream =>
      _recognizedTextController.stream;

}
