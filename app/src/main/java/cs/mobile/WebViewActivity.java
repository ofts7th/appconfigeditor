package cs.mobile;

import cs.util.JsBridge;
import cs.util.Session;
import cs.util.WebViewActivityUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class WebViewActivity extends Activity {
    protected WebViewActivityUtil util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.util = new WebViewActivityUtil(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInsanBundle) {
        super.onSaveInstanceState(savedInsanBundle);
        Session.saveStates(savedInsanBundle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInsanBundle) {
        super.onRestoreInstanceState(savedInsanBundle);
        Session.restoreStates(savedInsanBundle);
    }

    protected void startWebView() {
        WebView view = initWebView();
        view.loadUrl("file:///android_asset/index.htm");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private WebView webview;

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    private WebView initWebView() {
        webview = new WebView(this);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.addJavascriptInterface(new JsBridge(this.util), "local");
        webview.getSettings().setDomStorageEnabled(true);

        final Activity activity = this;
        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(activity).
                        setTitle("提示").setMessage(message).setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                result.confirm();
                            }
                        }).create().show();
                return true;//super.onJsConfirm(view, url, message, result);
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(activity);
                b.setTitle("提示");
                b.setMessage(message);
                b.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
                b.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.cancel();
                    }
                });
                b.create().show();
                return true;
            }
        });
        this.util.setWebView(webview);
        setContentView(webview);
        return webview;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.util.callJsFunc("goback()");
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
