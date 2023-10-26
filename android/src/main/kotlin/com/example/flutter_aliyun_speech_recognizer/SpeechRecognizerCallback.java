package com.example.flutter_aliyun_speech_recognizer;

public interface SpeechRecognizerCallback {
    void setRecognizingState(boolean state);

    void setRecognizedText(String text);
}
