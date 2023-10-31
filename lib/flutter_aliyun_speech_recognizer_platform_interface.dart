import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_aliyun_speech_recognizer_method_channel.dart';

abstract class FlutterAliyunSpeechRecognizerPlatform extends PlatformInterface {
  /// Constructs a FlutterAliyunSpeechRecognizerPlatform.
  FlutterAliyunSpeechRecognizerPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterAliyunSpeechRecognizerPlatform _instance = MethodChannelFlutterAliyunSpeechRecognizer();

  /// The default instance of [FlutterAliyunSpeechRecognizerPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterAliyunSpeechRecognizer].
  static FlutterAliyunSpeechRecognizerPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterAliyunSpeechRecognizerPlatform] when
  /// they register themselves.
  static set instance(FlutterAliyunSpeechRecognizerPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<void> start(String token) {
    throw UnimplementedError('start() has not been implemented.');
  }

  Future<void> stop() {
    throw UnimplementedError('stop() has not been implemented.');
  }

  Stream<bool> get recognizingStateStream {
    return const Stream.empty();
  }

  Stream<String> get recognizedTextStream {
    return const Stream.empty();
  }
}
