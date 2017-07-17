package com.test.sound;


import android.os.Bundle;
import android.app.Activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.content.Intent;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.test.sound.R;

/**
 * 音声入力（Input）と音声読み上げ（Output）のテスト。
 * マイクに入った音声を認識して，そのまま音声合成し，おうむ返しにスピーカ出力を試みる。
 * @author id:language_and_engineering
 *
 */
public class MainActivity extends Activity implements OnClickListener, TextToSpeech.OnInitListener
{
    // 音声入力用
    SpeechRecognizer sr;

    // 音声合成用
    TextToSpeech tts = null;

    ArrayList<String> soundList = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener( this );

        tts = new TextToSpeech(this, this);
    }


    @Override
    public void onClick(View v)
    {
        // 音声認識APIに自作リスナをセット
        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new MyRecognitionListener());

        // インテントを作成
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());

        // 入力言語のロケールを設定
        Spinner spinner  = (Spinner) findViewById(R.id.spinner);
        String item = (String) spinner.getSelectedItem();

        if("日本語".equals(item)){
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.JAPAN.toString());
        }else if("英語".equals(item)){
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString());
        }else if("中国語".equals(item)){
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINA.toString());
        }

        // 音声認識APIにインテントを処理させる
        sr.startListening(intent);
    }



    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS) {
            // 音声合成の設定を行う

            float pitch = 1.0f; // 音の高低
            float rate = 1.0f; // 話すスピード
            Locale locale = Locale.US; // 対象言語のロケール
            // ※ロケールの一覧表
            //   http://docs.oracle.com/javase/jp/1.5.0/api/java/util/Locale.html

            tts.setPitch(pitch);
            tts.setSpeechRate(rate);
            tts.setLanguage(locale);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if( tts != null )
        {
            // 破棄
            tts.shutdown();
        }
    }



    // 音声認識のリスナ
    class MyRecognitionListener implements RecognitionListener {

        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
        }

        @Override
        public void onEndOfSpeech() {
        }

        @Override
        public void onError(int error) {
            if(error == 7){
                Toast.makeText(getApplicationContext(), "エラー： " + error + "　入力を確認できませんでした", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(getApplicationContext(), "エラー： " + error, Toast.LENGTH_LONG).show();
            }
            // エラーコードの一覧表
            // http://developer.android.com/intl/ja/reference/android/speech/SpeechRecognizer.html#ERROR_AUDIO

            // 認識結果の候補が存在しなかった場合や，RECORD_AUDIOのパーミッションが不足している場合など
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
        }

        @Override
        public void onReadyForSpeech(Bundle params) {
            Toast.makeText(getApplicationContext(), "5秒以内に話しかけてください", Toast.LENGTH_LONG).show();
        }


        @Override
        public void onResults(Bundle results) {
            // 結果を受け取る
            ArrayList<String> candidates = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String s = candidates.get(0);

            soundList.add(0, getScreenData(s));

            ListView listView = (ListView) findViewById(R.id.soundList);
            listView.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, soundList));

            //setTextData(s);

            // トーストで結果を表示
            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();

            // 音声合成して発音
            if(tts.isSpeaking()) {
                tts.stop();
            }
            tts.speak(s, TextToSpeech.QUEUE_FLUSH, null);
        }


        @Override
        public void onRmsChanged(float rmsdB) {
        }

    }



    private String getScreenData(String text){
        StringBuilder sb = new StringBuilder();
        String now = DateFormat.format("MM/dd kk:mm:ss", Calendar.getInstance()).toString();

        sb.append(now);
        sb.append("　");
        sb.append(text);

        return  sb.toString();
    }

}