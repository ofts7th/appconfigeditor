package cs.util;

import android.webkit.JavascriptInterface;

public class JsBridge {
    private WebViewActivityUtil util;

    public JsBridge(WebViewActivityUtil util) {
        this.util = util;
    }

    @JavascriptInterface
    public String getVersion() {
        return Version.Version;
    }

    @JavascriptInterface
    public String getMachineId() {
        return Util.getDeviceId();
    }

    @JavascriptInterface
    public void getWebData(String k, String surl) {
        this.util.getWebData(k, surl);
    }

    @JavascriptInterface
    public void postWebData(String k, String surl, String data) {
        this.util.postWebData(k, surl, data);
    }

    @JavascriptInterface
    public String getWebResult(String k) {
        return this.util.getNetResponse(k);
    }

    @JavascriptInterface
    public void downloadFile(String fileUrl, String desPath) {
        this.util.downloadFile(fileUrl, desPath);
    }

    @JavascriptInterface
    public void uploadFile(String uploadUrl, String path) {
        this.util.uploadFile(uploadUrl, path);
    }

    @JavascriptInterface
    public String readAssetFile(String path) {
        return Util.readAssetFile(path);
    }

    @JavascriptInterface
    public void exit() {
        this.util.finishActivity();
    }

    @JavascriptInterface
    public String getConfig(String k) {
        return Util.getSafeConfig(k);
    }

    @JavascriptInterface
    public void saveConfig(String k, String v) {
        Util.saveConfig(k, v);
    }

    @JavascriptInterface
    public void installApk(String f) {
        this.util.installApk(f);
    }

    @JavascriptInterface
    public void exitApp() {
        System.exit(0);
    }

    @JavascriptInterface
    public String getConfigItemList(String project) {
        return this.util.getConfigItemList(project);
    }

    @JavascriptInterface
    public String getProjectList() {
        return this.util.getProjectList();
    }

    @JavascriptInterface
    public String readConfig(String file) {
        return this.util.readConfig(file);
    }

    @JavascriptInterface
    public void saveProjectConfig(String file, String content) {
        this.util.saveConfig(file, content);
    }

    @JavascriptInterface
    public void activeConfig(String project, String config) {
        this.util.activeConfig(project, config);
    }

    @JavascriptInterface
    public void deleteConfig(String config) {
        this.util.deleteConfig(config);
    }

    @JavascriptInterface
    public void copyConfig(String project, String config, String newName) {
        this.util.copyConfig(project, config, newName);
    }
}
