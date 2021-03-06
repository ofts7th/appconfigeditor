package cs.util;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.WebView;

import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import cs.mobile.WebViewActivity;
import cs.string;

public class WebViewActivityUtil {
    private WebViewActivity _activity;
    private WebView _view;

    public WebViewActivityUtil(WebViewActivity activity) {
        _activity = activity;
    }

    public void setWebView(WebView view) {
        _view = view;
    }

    public void showMessageInWebView(String msg) {
        this.callJsFunc("showMessage('" + msg + "')");
    }

    public void showMessageForProgress(String msg) {
        callJsFunc("cbProgressError('" + msg + "')");
    }

    private ArrayList<String> netResponseQueue = new ArrayList<String>();
    private HashMap<String, String> netResponseResult = new HashMap<String, String>();

    public String getNetResponse(String k) {
        String ret = netResponseResult.get(k);
        netResponseResult.remove(k);
        return ret;
    }

    public void setNetResponse(String k, String response) {
        netResponseResult.put(k, response);
        netResponseQueue.add(k);
        while (netResponseQueue.size() > 10) {
            String fk = netResponseQueue.get(0);
            netResponseQueue.remove(0);
            netResponseResult.remove(fk);
        }
    }

    public void callJsFunc(String func) {
        if (_view == null)
            return;

        _view.loadUrl("javascript:" + func);
    }

    private Handler netAccessHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                callJsFunc("execNetCallback('" + msg.obj.toString() + "')");
            } else {
                showMessageInWebView(msg.obj.toString());
            }
        }

        ;
    };

    public void logAsync(final String msg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //log(msg);
            }
        }).start();
    }

    public void log(String msg) {
        try {
            //getWebResult("http://10.10.10.66:8002/Log.aspx?msg="
            //		+ URLEncoder.encode(msg, "utf-8"));
        } catch (Exception ex) {

        }
    }

    public void getWebData(final String k, final String surl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(surl);
                    HttpURLConnection conn = (HttpURLConnection) url
                            .openConnection();
                    conn.setConnectTimeout(3000);
                    InputStream in = new BufferedInputStream(conn
                            .getInputStream());
                    String result = Util.readInStream(in);
                    in.close();

                    if (string.IsNullOrEmpty(k))
                        return;

                    setNetResponse(k, result);
                    Message msg = new Message();
                    msg.what = 0;
                    msg.obj = k;
                    netAccessHandler.sendMessage(msg);
                } catch (Exception e) {
                    Message msg = new Message();
                    msg.what = -1;
                    msg.obj = e.getMessage();
                    netAccessHandler.sendMessage(msg);
                }
            }
        }).start();
    }

    public void postWebData(final String k, final String surl,
                            final String data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(surl);
                    HttpURLConnection conn = (HttpURLConnection) url
                            .openConnection();
                    conn.setConnectTimeout(3000);
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.getOutputStream().write(data.getBytes());

                    InputStream in = new BufferedInputStream(conn
                            .getInputStream());
                    String result = Util.readInStream(in);
                    in.close();

                    if (string.IsNullOrEmpty(k))
                        return;

                    setNetResponse(k, result);
                    Message msg = new Message();
                    msg.what = 0;
                    msg.obj = k;
                    netAccessHandler.sendMessage(msg);
                } catch (Exception e) {
                    Message msg = new Message();
                    msg.what = -1;
                    msg.obj = e.getMessage();
                    netAccessHandler.sendMessage(msg);
                }
            }
        }).start();
    }

    private final int progressErrorCode_Error = -1;
    private final int progressErrorCode_FileTooBig = -2;
    private final int progressCode_DownloadComplete = 200;
    private final int progressErrorCode_UploadComplete = 300;

    private Handler progressHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case progressErrorCode_Error:
                    showMessageForProgress("出现错误");
                    return;
                case progressErrorCode_FileTooBig:
                    showMessageForProgress("文件太大");
                    return;
                case progressCode_DownloadComplete:
                    callJsFunc("execDownloadCompleteCallback()");
                    return;
                case progressErrorCode_UploadComplete:
                    callJsFunc("execUploadCompleteCallback('"
                            + msg.obj.toString() + "')");
                    return;
            }
            callJsFunc("execProgressCallback(" + msg.what + ")");
        }

        ;
    };

    public void downloadFile(final String fileUrl, final String desPath) {
        if (Util.getRootDir() == null)
            return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String f = desPath;
                    if (desPath.startsWith("/")) {
                        f = desPath.substring(1);
                    }
                    URL url = new URL(fileUrl);
                    HttpURLConnection conn = (HttpURLConnection) url
                            .openConnection();
                    conn.connect();
                    int length = conn.getContentLength();
                    InputStream is = conn.getInputStream();

                    File dir = new File(Util.getRootDir(), f.substring(0,
                            f.lastIndexOf('/')));
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File file = new File(Util.getRootDir(), f);
                    if (file.exists()) {
                        file.delete();
                    }

                    FileOutputStream fos = new FileOutputStream(file);

                    int count = 0;
                    byte buf[] = new byte[1024];

                    while (true) {
                        int numread = is.read(buf);
                        count += numread;
                        progressHandler
                                .sendEmptyMessage((int) (((float) count / length) * 100));
                        if (numread <= 0) {
                            progressHandler
                                    .sendEmptyMessage(progressCode_DownloadComplete);
                            break;
                        }
                        fos.write(buf, 0, numread);
                    }
                    fos.close();
                    is.close();
                } catch (Exception e) {
                    progressHandler.sendEmptyMessage(progressErrorCode_Error);
                }
            }
        }).start();
    }

    public void uploadFile(final String uploadUrl, final String path) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String end = "\r\n";
                    String Hyphens = "--";
                    String boundary = "*****";
                    URL url = new URL(uploadUrl);
                    HttpURLConnection conn = (HttpURLConnection) url
                            .openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("Charset", "UTF-8");
                    conn.setRequestProperty("Content-Type",
                            "multipart/form-data;boundary=" + boundary);

                    DataOutputStream os = new DataOutputStream(conn
                            .getOutputStream());
                    os.writeBytes(Hyphens + boundary + end);

                    os.writeBytes("Content-Disposition:form-data;name=\"file1\";filename=\""
                            + path.substring(path.lastIndexOf('/') + 1)
                            + "\""
                            + end);
                    os.writeBytes(end);

                    File file = new File(path);
                    long length = file.length();
                    if (length > 100000000) {
                        progressHandler
                                .sendEmptyMessage(progressErrorCode_FileTooBig);
                        return;
                    }

                    int count = 0;
                    byte buf[] = new byte[1024];
                    FileInputStream is = new FileInputStream(file);
                    while (true) {
                        int numread = is.read(buf);
                        count += numread;
                        progressHandler
                                .sendEmptyMessage((int) (((float) count / length) * 100));
                        if (numread <= 0) {
                            os.writeBytes(end);
                            os.writeBytes(Hyphens + boundary + Hyphens + end);
                            is.close();
                            os.flush();
                            os.close();

                            String result = Util.readInStream(conn
                                    .getInputStream());
                            Message msg = new Message();
                            msg.what = progressErrorCode_UploadComplete;
                            msg.obj = result;
                            progressHandler.sendMessage(msg);
                            break;
                        } else {
                            os.write(buf, 0, numread);
                        }
                    }
                } catch (Exception e) {
                    progressHandler.sendEmptyMessage(progressErrorCode_Error);
                }
            }
        }).start();
    }

    public void installApk(String f) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        if (f.startsWith("/")) {
            f = f.substring(1);
        }
        String filePath = Util.getRootDir().getAbsolutePath() + "/" + f;
        i.setDataAndType(Uri.parse("file://" + filePath),
                "application/vnd.android.package-archive");
        this._activity.startActivity(i);
        System.exit(0);
    }

    public void finishActivity() {
        this._activity.finish();
    }

    public String getProjectList() {
        JSONArray arrData = new JSONArray();
        File configDir = new File(Util.getExRootDir(), "sdyouda/config");
        if (configDir.exists()) {
            File[] children = configDir.listFiles();
            if (children != null) {
                for (File project : children) {
                    arrData.put(project.getName());
                }
            }
        }
        return arrData.toString();
    }

    public String getConfigItemList(String project) {
        JSONArray arrData = new JSONArray();
        File configDir = new File(Util.getExRootDir(), "sdyouda/config/" + project);
        if (configDir.exists()) {
            File[] children = configDir.listFiles();
            if (children != null) {
                for (File c : children) {
                    arrData.put(c.getName());
                }
            }
        }
        return arrData.toString();
    }

    public String readConfig(String file) {
        String ret = Util.readFile(Util.getExRootDir() + "/sdyouda/config/" + file, true);
        return ret;
    }

    public void saveConfig(String file, String content) {
        Util.writeFile(Util.getExRootDir() + "/sdyouda/config/" + file, content);
    }

    public void activeConfig(String proj, String conf) {
        Util.deleteFile(Util.getExRootDir() + "/sdyouda/config/config.ini");
        Util.copyFile(Util.getExRootDir() + "/sdyouda/config/" + proj + "/" + conf, Util.getExRootDir() + "/sdyouda/config/" + proj + "/config.ini");
    }

    public void deleteConfig(String conf) {
        Util.deleteFile(Util.getExRootDir() + "/sdyouda/config/" + conf);
    }

    public void copyConfig(String proj, String conf, String newConfig) {
        Util.copyFile(Util.getExRootDir() + "/sdyouda/config/" + proj + "/" + conf, Util.getExRootDir() + "/sdyouda/config/" + proj + "/" + newConfig + ".ini");
    }
}