import 'package:flutter/material.dart';
import 'dart:async';
import 'package:flutter_aliyun_speech_recognizer/flutter_aliyun_speech_recognizer.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _flutterAliyunSpeechRecognizerPlugin = FlutterAliyunSpeechRecognizer();
  bool _started = false;
  String _result = '...';
  int _clearPoint = 0;

  @override
  void initState() {
    super.initState();
  }

  String tempToken = "0408ce1afa0949f891009496f1af52c6";

  Future<void> start() async {
    try {
      await _flutterAliyunSpeechRecognizerPlugin.start(tempToken);
    } on Exception {
      _started = false;
    }
  }

  Future<void> stop() async {
    try {
      await _flutterAliyunSpeechRecognizerPlugin.stop();
    } on Exception {
      debugPrint('stop failed');
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      theme: ThemeData(useMaterial3: true),
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Aliyun Speech Recognizer Plugin'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              StreamBuilder<bool>(
                  initialData: _started,
                  stream: _flutterAliyunSpeechRecognizerPlugin
                      .recognizingStateStream,
                  builder: (context, snapshot) {
                    if (snapshot.hasData) {
                      _started = snapshot.data!;
                      if (!_started) {
                        _clearPoint = 0;
                      }
                    } else {
                      return const Text('recognizingState: null');
                    }
                    return Column(
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                          children: [
                            ElevatedButton(
                              onPressed: (_started)
                                  ? null
                                  : () {
                                      start();
                                    },
                              child: const Text('Start'),
                            ),
                            ElevatedButton(
                              onPressed: (_started)
                                  ? () {
                                      stop();
                                    }
                                  : null,
                              child: const Text('Stop'),
                            ),
                          ],
                        ),
                        Text('recognizingState: $_started'),
                      ],
                    );
                  }),
              const Divider(height: 20),
              StreamBuilder<String>(
                stream:
                    _flutterAliyunSpeechRecognizerPlugin.recognizedTextStream,
                builder: (context, snapshot) {
                  if (snapshot.hasData) {
                    _result = snapshot.data!;
                    return Text('recognizedText: ${_result.substring(_clearPoint)}');
                  } else {
                    return const Text('recognizedText: null');
                  }
                },
              ),
              ElevatedButton(
                onPressed: () {
                  if (_result.length > _clearPoint) {
                    setState(() {
                      _clearPoint = _result.length;
                    });
                  }
                },
                child: const Text('Clear'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
