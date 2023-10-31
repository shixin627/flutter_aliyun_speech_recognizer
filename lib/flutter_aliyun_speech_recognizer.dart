
import 'flutter_aliyun_speech_recognizer_platform_interface.dart';

class FlutterAliyunSpeechRecognizer {
  Future<void> start(String token) {
    return FlutterAliyunSpeechRecognizerPlatform.instance.start(token);
  }

  Future<void> stop() {
    return FlutterAliyunSpeechRecognizerPlatform.instance.stop();
  }

  Stream<bool> get recognizingStateStream {
    return FlutterAliyunSpeechRecognizerPlatform.instance.recognizingStateStream;
  }

  Stream<String> get recognizedTextStream {
    return FlutterAliyunSpeechRecognizerPlatform.instance.recognizedTextStream;
  }
}
