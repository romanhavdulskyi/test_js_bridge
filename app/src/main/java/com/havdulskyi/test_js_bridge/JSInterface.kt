package com.havdulskyi.test_js_bridge

import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.FragmentActivity

class JSInterface(
    private val webView: WebView,
    activity: FragmentActivity
) {

    private val dummyQrCallback: (String) -> Unit = {

    }

    private var qrTemporaryCallback: (String) -> Unit = dummyQrCallback

    private val qrActivityResultLauncher: ActivityResultLauncher<QrContract.NoParam> =
        activity.registerForActivityResult(QrContract()) { result ->
            if (result is QrContract.Action.QrResult)
                qrTemporaryCallback(result.data)
            else
                resetQrCallback()
        }

    @JavascriptInterface
    fun openQRScanner(callbackFunction: String) {
        qrTemporaryCallback = createQrCallback(callbackFunction)
        qrActivityResultLauncher.launch(QrContract.NoParam)
    }

    private fun createQrCallback(callbackFunction: String): (String) -> Unit {
        val callback: (String) -> Unit = { result ->
            WebViewUtils.callJavaScript(webView, callbackFunction, result)
            resetQrCallback()
        }
        return callback
    }

    private fun resetQrCallback() {
        qrTemporaryCallback = dummyQrCallback
    }

}