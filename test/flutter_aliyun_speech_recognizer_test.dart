import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_aliyun_speech_recognizer/flutter_aliyun_speech_recognizer.dart';
import 'package:flutter_aliyun_speech_recognizer/flutter_aliyun_speech_recognizer_platform_interface.dart';
import 'package:flutter_aliyun_speech_recognizer/flutter_aliyun_speech_recognizer_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockFlutterAliyunSpeechRecognizerPlatform
    with MockPlatformInterfaceMixin
    implements FlutterAliyunSpeechRecognizerPlatform {

  @override
  Future<String?> start() => Future.value('42');
}

void main() {
  final FlutterAliyunSpeechRecognizerPlatform initialPlatform = FlutterAliyunSpeechRecognizerPlatform.instance;

  test('$MethodChannelFlutterAliyunSpeechRecognizer is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelFlutterAliyunSpeechRecognizer>());
  });

  test('getPlatformVersion', () async {
    FlutterAliyunSpeechRecognizer flutterAliyunSpeechRecognizerPlugin = FlutterAliyunSpeechRecognizer();
    MockFlutterAliyunSpeechRecognizerPlatform fakePlatform = MockFlutterAliyunSpeechRecognizerPlatform();
    FlutterAliyunSpeechRecognizerPlatform.instance = fakePlatform;

    expect(await flutterAliyunSpeechRecognizerPlugin.start(), '42');
  });
}
