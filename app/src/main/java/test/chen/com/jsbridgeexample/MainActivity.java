package test.chen.com.jsbridgeexample;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.widget.Button;

import com.czhzero.jsbridge.BridgeHandler;
import com.czhzero.jsbridge.BridgeWebView;
import com.czhzero.jsbridge.CallBackFunction;
import com.czhzero.jsbridge.DefaultHandler;
import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "MainActivity";

    BridgeWebView webView;

    Button button;

    int RESULT_CODE = 0;

    ValueCallback<Uri> mUploadMessage;

    static class Location {
        String address;
    }

    static class User {
        String name;
        Location location;
        String testStr;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        webView = (BridgeWebView) findViewById(R.id.webView);

        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
        findViewById(R.id.button3).setOnClickListener(this);

        webView.setDefaultHandler(new DefaultHandler());

        webView.setWebChromeClient(new WebChromeClient() {

            @SuppressWarnings("unused")
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType, String capture) {
                this.openFileChooser(uploadMsg);
            }

            @SuppressWarnings("unused")
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType) {
                this.openFileChooser(uploadMsg);
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                mUploadMessage = uploadMsg;
                pickFile();
            }
        });

        webView.loadUrl("file:///android_asset/demo.html");

        webView.registerHandler("submitFromWeb", new BridgeHandler() {

            @Override
            public void handler(String data, CallBackFunction function) {
                Log.i(TAG, "handler = submitFromWeb, data from web = " + data);
                function.onCallBack("Native处理过后的=" + data);
            }

        });


    }

    public void pickFile() {
        Intent chooserIntent = new Intent(Intent.ACTION_GET_CONTENT);
        chooserIntent.setType("image/*");
        startActivityForResult(chooserIntent, RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == RESULT_CODE) {
            if (null == mUploadMessage) {
                return;
            }
            Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
    }

    @Override
    public void onClick(View v) {

        Log.i(TAG, "send data to js");

        switch (v.getId()) {
            case R.id.button1:

                webView.send("hello");

                break;
            case R.id.button2:

                webView.callHandler("functionInJs", "String Data from Native", new CallBackFunction() {

                    @Override
                    public void onCallBack(String data) {
                        Log.i(TAG, "reponse data from js " + data);
                    }

                });

                break;
            case R.id.button3:

                User user = new User();
                Location location = new Location();
                location.address = "SDU";
                user.location = location;
                user.name = "大头鬼";

                webView.callHandler("functionInJs", new Gson().toJson(user), new CallBackFunction() {
                    @Override
                    public void onCallBack(String data) {
                        Log.i(TAG, "reponse data from js " + data);
                    }
                });

                break;
        }


    }
}
