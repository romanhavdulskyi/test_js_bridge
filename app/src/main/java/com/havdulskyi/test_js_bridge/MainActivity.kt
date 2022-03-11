package com.havdulskyi.test_js_bridge

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private var webView : WebView? = null
    private var jsInterface : JSInterface? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.wv_browser)
        webView?.let {
            web ->
            val js = JSInterface(web, this)
            jsInterface = js
            web.settings.javaScriptCanOpenWindowsAutomatically = true
            web.settings.javaScriptEnabled = true
            web.addJavascriptInterface(js, "Android")
            web.webChromeClient = WebChromeClient()
        }
        webView?.loadUrl("file:///android_asset/web_page.html")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        jsInterface?.handlePermissionResult(requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(this.javaClass.simpleName, "onActivityResult $requestCode $resultCode $data")
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        try {
            webView?.destroy()
        }catch (e : Exception) {
            e.printStackTrace()
        }finally {
            webView = null
        }
        super.onDestroy()
    }
}