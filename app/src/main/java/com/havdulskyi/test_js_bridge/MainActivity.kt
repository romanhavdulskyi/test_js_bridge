package com.havdulskyi.test_js_bridge

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import java.lang.Exception

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
        }
        webView?.loadUrl("file:///android_asset/web_page.html")
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