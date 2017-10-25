package com.example.tangyubao.speechthree;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;


import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements EventListener {
    protected TextView txtLog;
    protected TextView txtResult;
    protected Button btn;
    protected Button stopBtn;
    private static String DESC_TEXT = "精简版识别，带有SDK唤醒运行的最少代码，仅仅展示如何调用，\n" +
            "也可以用来反馈测试SDK输入参数及输出回调。\n" +
            "本示例需要自行根据文档填写参数，可以使用之前识别示例中的日志中的参数。\n" +
            "需要完整版请参见之前的识别示例。\n" +
            "需要测试离线命令词识别功能可以将本类中的enableOffline改成true，首次测试离线命令词请联网使用。之后请说出“打电话给张三”";

    private EventManager asr;

    private boolean logTime = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_mini);
        initView();
        initPermission();
        asr = EventManagerFactory.create(this, "asr");
        asr.registerListener(this); //  EventListener 中 onEvent方法
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                start();
            }
        });
        stopBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                stop();
            }
        });
        System.out.println("MainActivity---onCreate()---stop()结束");

    }

    private void initView() {
        txtResult = (TextView) findViewById(R.id.txtResult);
        txtLog = (TextView) findViewById(R.id.txtLog);
        btn = (Button) findViewById(R.id.btn);
        stopBtn = (Button) findViewById(R.id.btn_stop);
        txtLog.setText(DESC_TEXT + "\n");
    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String permissions[] = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm :permissions){
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                //进入到这里代表没有权限.

            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()){
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }

    @Override
    public void onEvent(String name, String params, byte[] data, int offset, int length) {
        System.out.println("MainActivity--------------onEvent()");
        String best_result="";
        String stdStr="asr.partial";
        if(name.equals(stdStr)){
            System.out.println("MainActivity-----onEvent----name.queals(stdStr)="+name.equals(stdStr));
            //String logTxt = "name: " + name;
            String logTxt = "";

            if (params != null && !params.isEmpty()) {
                System.out.println("MainActivity-----onEvent----params"+params);
                String[] temp=params.split("\"best_result\":\"");
                System.out.println("MainActivity-----onEvent----params=temp[1]="+params);
                String[] temp2=temp[1].split("\",\"results_recognition");
                params=temp2[0];
                System.out.println("MainActivity-----onEvent----params=temp2[0]="+params);
               // String[] temp3=params.split("\",\"result_recognition");
              //  params=temp3[0];
                //System.out.println("MainActivity-----onEvent----params=temp3[0]="+params);
                //logTxt += " ;params :" + params;
                logTxt +=params;
            }
            if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {
                if (params.contains("\"nlu_result\"")) {
                    if (length > 0 && data.length > 0) {
                        logTxt += ", 语义解析结果：" + new String(data, offset, length);
                    }
                }
            } else if (data != null) {
                logTxt += " ;data length=" + data.length;
            }
            printLog(logTxt);
            System.out.println("MainActivity--------------onEvent()----logTxt"+logTxt);
        }
    }
    /**
     * 测试参数填在这里
     */
    private void start() {
        System.out.println("MainActivity---start()--------开始");
        txtLog.setText("");
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        String event = null;
        event = SpeechConstant.ASR_START; // 替换成测试的event

        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        params.put(SpeechConstant.VAD,SpeechConstant.VAD_DNN);
//        if (enableOffline){
//            params.put(SpeechConstant.DECODER, 2);
//        }
        //  params.put(SpeechConstant.NLU, "enable");
        // params.put(SpeechConstant.VAD_ENDPOINT_TIMEOUT, 800);
        // params.put(SpeechConstant.VAD, SpeechConstant.VAD_DNN);
        //  params.put(SpeechConstant.PROP ,20000);
        String json = null; //可以替换成自己的json
        json = new JSONObject(params).toString(); // 这里可以替换成你需要测试的json
        asr.send(event, json, null, 0, 0);
        printLog("start()-----输入参数：" + json);
        System.out.println("MainActivity---start-----结束---------json-----------"+json);
    }

        private void stop() {
            System.out.println("MainActivity---stop（）---------------------开始");
        asr.send(SpeechConstant.ASR_STOP, null, null, 0, 0); //
    }
    private void printLog(String text) {
        if (logTime) {
            //text += "  ;time=" + System.currentTimeMillis();
        }
        System.out.println("printLog()-----------------text----------"+text);
        text += "\n";
        Log.i(getClass().getName(), text);
        txtLog.append(text + "\n");
    }
}
