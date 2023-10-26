package com.example.flutter_aliyun_speech_recognizer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.idst.nui.AsrResult;
import com.alibaba.idst.nui.CommonUtils;
import com.alibaba.idst.nui.Constants;
import com.alibaba.idst.nui.INativeNuiCallback;
import com.alibaba.idst.nui.KwsResult;
import com.alibaba.idst.nui.NativeNui;
import java.util.concurrent.atomic.AtomicBoolean;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

// 本样例展示在线一句话语音识别使用方法
// Android SDK 详细说明：https://help.aliyun.com/document_detail/173115.html
public class SpeechRecognizerModule implements INativeNuiCallback {
    private SpeechRecognizerCallback callback;

    public SpeechRecognizerModule(SpeechRecognizerCallback callback) {
        this.callback = callback;
    }

    private void setRecognizingState(boolean state) {
        if (callback != null) {
            callback.setRecognizingState(state);
        }
    }

    private void setRecognizedText(String result) {
        Object obj = (Object) JSONObject.parse(result);
        JSONObject jsonObject = (JSONObject) obj;
        String payloadResult = jsonObject.getJSONObject("payload").getString("result");
        if (callback != null) {
            Log.i(TAG, "setRecognizedText: " + payloadResult);
            callback.setRecognizedText(payloadResult);
        }
    }

    private static final String TAG = "SpeechRecognizer";
    NativeNui nui_instance = new NativeNui();
    final static int WAVE_FRAM_SIZE = 20 * 2 * 1 * 16000 / 1000; //20ms audio for 16k/16bit/mono
    public final static int SAMPLE_RATE = 16000;
    private AudioRecord mAudioRecorder;
    private AtomicBoolean vadMode = new AtomicBoolean(false);
    private HandlerThread mHanderThread;
    private boolean mInit = false;
    private Handler mHandler;
    private MediaPlayer mp;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    public void checkPermission(Activity activity, Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查该权限是否已经获取
            int i = ContextCompat.checkSelfPermission(context, permissions[0]);
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            if (i != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(
                        activity,
                        permissions,
                        321
                );
            }
            while (true) {
                i = ContextCompat.checkSelfPermission(context, permissions[0]);
                // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
                if (i == PackageManager.PERMISSION_GRANTED)
                    break;
            }
        }
    }

    public void startRecognition() {
        setRecognizingState(true);
        startDialog();
    }

    private void startDialog() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                //由于对外的SDK不带有本地VAD模块(仅带有唤醒功能的SDK具有VAD模块)，
                //若要使用VAD模式，则需要设置nls_config参数启动在线VAD模式(见genParams())
                Constants.VadMode vad_mode = Constants.VadMode.TYPE_P2T;
                //设置相关识别参数，具体参考API文档
                //  initialize()之后startDialog之前调用
                nui_instance.setParams(genParams());
                int ret = nui_instance.startDialog(vad_mode,
                        genDialogParams());
                Log.i(TAG, "start done with " + ret);
                if (ret != 0) {
                    final String msg_text = Utils.getMsgWithErrorCode(ret, "start");
                }
            }
        });
    }

    private String genDialogParams() {
        String params = "";
        try {
            JSONObject dialog_param = new JSONObject();
            //运行过程中可以在startDialog时更新参数，尤其是更新过期token
//            dialog_param.put("app_key", "");
//            dialog_param.put("token", "");
            params = dialog_param.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i(TAG, "dialog params: " + params);
        return params;
    }

    public void stopRecognition() {
        setRecognizingState(false);
        if (!checkInitialization()) {
            return;
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                long ret = nui_instance.stopDialog();
                Log.i(TAG, "cancel dialog " + ret + " end");
            }
        });
    }

    private boolean checkInitialization() {
        if (!mInit) {
            return false;
        } else {
            return true;
        }
    }

    public void releaseNuiInstance() {
        nui_instance.release();
    }

    public void doInit(Context context) {
        String version = NativeNui.GetInstance().GetVersion();
        Log.i(TAG, "current sdk version: " + version);
        final String version_text = "内部SDK版本号:" + version;

        mHanderThread = new HandlerThread("process_thread");
        mHanderThread.start();
        mHandler = new Handler(mHanderThread.getLooper());

        Log.i(TAG, "vad mode onCheckedChanged true");
        vadMode.set(true);
        setRecognizingState(false);

        //获取工作路径, 这里获得当前nuisdk.aar中assets路径
        String asset_path = CommonUtils.getModelPath(context);
        Log.i(TAG, "use workspace " + asset_path);

        String debug_path = context.getExternalCacheDir().getAbsolutePath() + "/debug_" + System.currentTimeMillis();
        Utils.createDir(debug_path);

        //录音初始化，录音参数中格式只支持16bit/单通道，采样率支持8K/16K
        //使用者请根据实际情况选择Android设备的MediaRecorder.AudioSource
        //录音麦克风如何选择,可查看https://developer.android.google.cn/reference/android/media/MediaRecorder.AudioSource
        mAudioRecorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, WAVE_FRAM_SIZE * 4);

        //这里主动调用完成SDK配置文件的拷贝
        if (CommonUtils.copyAssetsData(context)) {
            Log.i(TAG, "copy assets data done");
        } else {
            Log.i(TAG, "copy assets failed");
            return;
        }

        //初始化SDK，注意用户需要在Auth.getAliYunTicket中填入相关ID信息才可以使用。
        int ret = nui_instance.initialize(this, genInitParams(asset_path, debug_path), Constants.LogLevel.LOG_LEVEL_VERBOSE, true);
        Log.i(TAG, "result = " + ret);
        if (ret == Constants.NuiResultCode.SUCCESS) {
            mInit = true;
        } else {
            final String msg_text = Utils.getMsgWithErrorCode(ret, "init");
            Log.e(TAG, msg_text);
        }
    }

    private String genParams() {
        String params = "";
        try {
            JSONObject nls_config = new JSONObject();
            nls_config.put("enable_intermediate_result", true);

            //参数可根据实际业务进行配置
            //接口说明可见: https://help.aliyun.com/document_detail/173298.html
            //查看 2.开始识别

            //由于对外的SDK不带有本地VAD模块(仅带有唤醒功能的SDK具有VAD模块)，
            //若要使用VAD模式，则需要设置nls_config参数启动在线VAD模式(见genParams())
            if (vadMode.get()) {
                nls_config.put("enable_voice_detection", true);
                nls_config.put("max_start_silence", 10000);
                nls_config.put("max_end_silence", 800);
            }

            //nls_config.put("enable_punctuation_prediction", true);
            //nls_config.put("enable_inverse_text_normalization", true);
            //nls_config.put("enable_voice_detection", true);
            //nls_config.put("customization_id", "test_id");
            //nls_config.put("vocabulary_id", "test_id");
            //nls_config.put("max_start_silence", 10000);
            //nls_config.put("max_end_silence", 800);
            //nls_config.put("sample_rate", 16000);
            //nls_config.put("sr_format", "opus");

            JSONObject parameters = new JSONObject();

            parameters.put("nls_config", nls_config);
            parameters.put("service_type", Constants.kServiceTypeASR); // 必填

            //如果有HttpDns则可进行设置
            //parameters.put("direct_ip", Utils.getDirectIp());

            params = parameters.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return params;
    }

    private String genInitParams(String workpath, String debugpath) {
        String str = "";
        try {
            //获取token方式：

            JSONObject object = new JSONObject();

            //账号和项目创建
            //  ak_id ak_secret app_key如何获得,请查看https://help.aliyun.com/document_detail/72138.html
            object.put("app_key", "aXhnERs5FGScnBZa"); // 必填

            //方法1：
            //  首先ak_id ak_secret app_key如何获得,请查看https://help.aliyun.com/document_detail/72138.html
            //  然后请看 https://help.aliyun.com/document_detail/466615.html 使用其中方案一获取临时凭证
            //  此方案简介: 远端服务器生成具有有效时限的临时凭证, 下发给移动端进行使用, 保证账号信息ak_id和ak_secret不被泄露
            //  获得Token方法(运行在APP服务端): https://help.aliyun.com/document_detail/450255.html?spm=a2c4g.72153.0.0.79176297EyBj4k

            object.put("token", "2be3bdcb658646f6866090d956463583");  // 必填
            //方法2：
            //  STS获取临时凭证方法暂不支持

            //方法3：（强烈不推荐，存在阿里云账号泄露风险）
            //  参考Auth类的实现在端上访问阿里云Token服务获取SDK进行获取。请勿将ak/sk存在本地或端侧环境。
            //  此方法优点: 端侧获得Token, 无需搭建APP服务器。
            //  此方法缺点: 端侧获得ak/sk账号信息, 极易泄露。
//            JSONObject object = Auth.getAliYunTicket();

            object.put("device_id", "skaiwalk"); // 必填, 推荐填入具有唯一性的id, 方便定位问题
            // 國外阿里雲帳號
            object.put("url", "wss://nls-gateway-ap-southeast-1.aliyuncs.com/ws/v1"); // 外网访问
//            object.put("url", "ws://nls-gateway.ap-southeast-1-internal.aliyuncs.com:80/ws/v1"); // 上海ECS内网访问
            // 國內阿里雲帳號
//            object.put("url", "wss://nls-gateway.cn-shanghai.aliyuncs.com:443/ws/v1"); // 默认
            object.put("workspace", workpath); // 必填, 且需要有读写权限

            object.put("sample_rate", "16000");
            object.put("format", "opus");
            //当初始化SDK时的save_log参数取值为true时，该参数生效。表示是否保存音频debug，该数据保存在debug目录中，需要确保debug_path有效可写。
//            object.put("save_wav", "true");
            //debug目录，当初始化SDK时的save_log参数取值为true时，该目录用于保存中间音频文件。
            object.put("debug_path", debugpath);

            // FullMix = 0   // 选用此模式开启本地功能并需要进行鉴权注册
            // FullCloud = 1
            // FullLocal = 2 // 选用此模式开启本地功能并需要进行鉴权注册
            // AsrMix = 3    // 选用此模式开启本地功能并需要进行鉴权注册
            // AsrCloud = 4
            // AsrLocal = 5  // 选用此模式开启本地功能并需要进行鉴权注册
            object.put("service_mode", Constants.ModeAsrCloud); // 必填
            str = object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i(TAG, "InsideUserContext:" + str);
        return str;
    }

    //当回调事件发生时调用
    @Override
    public void onNuiEventCallback(Constants.NuiEvent event, final int resultCode, final int arg2, KwsResult kwsResult,
                                   AsrResult asrResult) {
        Log.i(TAG, "event=" + event);
        if (event == Constants.NuiEvent.EVENT_ASR_RESULT) {
            Log.d(TAG, "asrResult=" + asrResult.asrResult);
            setRecognizedText(asrResult.asrResult);
            setRecognizingState(false);
        } else if (event == Constants.NuiEvent.EVENT_ASR_PARTIAL_RESULT) {
            setRecognizedText(asrResult.asrResult);
        } else if (event == Constants.NuiEvent.EVENT_ASR_ERROR) {
            setRecognizedText(asrResult.asrResult);
            final String msg_text = Utils.getMsgWithErrorCode(resultCode, "start");
            setRecognizingState(false);
        } else if (event == Constants.NuiEvent.EVENT_DIALOG_EX) { /* unused */
            Log.i(TAG, "dialog extra message = " + asrResult.asrResult);
        }
    }

    //当调用NativeNui的start后，会一定时间反复回调该接口，底层会提供buffer并告知这次需要数据的长度
    //返回值告知底层读了多少数据，应该尽量保证return的长度等于需要的长度，如果返回<=0，则表示出错
    @Override
    public int onNuiNeedAudioData(byte[] buffer, int len) {
        int ret = 0;
        if (mAudioRecorder.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "audio recorder not init");
            return -1;
        }
        ret = mAudioRecorder.read(buffer, 0, len);
        return ret;
    }

    //当录音状态发送变化的时候调用
    @Override
    public void onNuiAudioStateChanged(Constants.AudioState state) {
        Log.i(TAG, "onNuiAudioStateChanged");
        if (state == Constants.AudioState.STATE_OPEN) {
            Log.i(TAG, "audio recorder start");
            mAudioRecorder.startRecording();
            Log.i(TAG, "audio recorder start done");
        } else if (state == Constants.AudioState.STATE_CLOSE) {
            Log.i(TAG, "audio recorder close");
            mAudioRecorder.release();
        } else if (state == Constants.AudioState.STATE_PAUSE) {
            Log.i(TAG, "audio recorder pause");
            mAudioRecorder.stop();
        }
    }

    @Override
    public void onNuiAudioRMSChanged(float val) {
//        Log.i(TAG, "onNuiAudioRMSChanged vol " + val);
    }

    @Override
    public void onNuiVprEventCallback(Constants.NuiVprEvent event) {
        Log.i(TAG, "onNuiVprEventCallback event " + event);
    }
}

