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

  @override
  void initState() {
    super.initState();
  }

  Future<void> start() async {
    if (_started) return;
    try {
      await _flutterAliyunSpeechRecognizerPlugin.start();
    } on Exception {
      _started = false;
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _started = true;
    });
  }

  Future<void> stop() async {
    if (!_started) return;
    try {
      await _flutterAliyunSpeechRecognizerPlugin.stop();
    } on Exception {
      debugPrint('stop failed');
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _started = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      theme: ThemeData(useMaterial3: true),
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: [
              StreamBuilder<bool>(
                stream: _flutterAliyunSpeechRecognizerPlugin.recognizingStateStream,
                builder: (context, snapshot) {
                  if (snapshot.hasData) {
                    _started = snapshot.data!;
                  } else {
                    return const Text('recognizingState: null');
                  }
                  return Column(
                    children: [
                      Row(
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
                }
              ),
              const Divider(),
              StreamBuilder<String>(
                stream: _flutterAliyunSpeechRecognizerPlugin.recognizedTextStream,
                builder: (context, snapshot) {
                  if (snapshot.hasData) {
                    return Text('recognizedText: ${snapshot.data}');
                  } else {
                    return const Text('recognizedText: null');
                  }
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}
