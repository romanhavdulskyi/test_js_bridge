package com.havdulskyi.test_js_bridge;

import android.webkit.WebView;

/**
 * Utility functions for Webview.
 *
 * @author Bertrand Martel
 */
public class WebViewUtils {

    private final static String TAG = WebViewUtils.class.getSimpleName();

    /**
     * Call javascript functions in webview.
     *
     * @param webView    webview object
     * @param methodName function name
     * @param params     function parameters
     */
    public static void callJavaScript(final WebView webView, String methodName, Object... params) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("javascript:try{");
        stringBuilder.append(methodName);
        stringBuilder.append("(");
        String separator = "";
        for (Object param : params) {
            stringBuilder.append(separator);
            separator = ",";
            if (param instanceof String) {
                stringBuilder.append("'");
            }
            stringBuilder.append(param);
            if (param instanceof String) {
                stringBuilder.append("'");
            }

        }
        stringBuilder.append(")}catch(error){console.error(error.message);}");
        final String call = stringBuilder.toString();

        webView.loadUrl(call);
    }

    /**
     * Call javascript functions in webview thread.
     *
     * @param webView    webview object
     * @param methodName function name
     * @param params     function parameters
     */
    public static void callOnWebViewThread(final WebView webView, final String methodName, final Object... params) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                WebViewUtils.callJavaScript(webView, methodName, params);
            }
        });
    }

}
