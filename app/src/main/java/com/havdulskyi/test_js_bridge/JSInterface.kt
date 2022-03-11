package com.havdulskyi.test_js_bridge

import android.Manifest
import android.content.Context.WIFI_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import com.google.gson.Gson
import java.lang.ref.WeakReference


class JSInterface(
    private val webView: WebView,
    activity: FragmentActivity
) {

    private val context = activity.applicationContext
    private val activityWeakReference = WeakReference(activity)

    private var enablingWifiState: EnablingWifiState = EnablingWifiState.NONE

    enum class EnablingWifiState{
        GRANTING_PERMISSIONS, ENABLING, NONE
    }

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

    @JavascriptInterface
    fun getWifiNetwork(): String {
        val networks = if (hasWifiPermissions()) {
            try {
                val service = wifiManager
                if (service.isWifiEnabled) {
                    if(enablingWifiState != EnablingWifiState.NONE)
                        enablingWifiState = EnablingWifiState.NONE
                    val results = service.scanResults
                    results.map { it.SSID }
                } else {
                    enableWifi()
                    listOf()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                listOf<String>()
            }
        } else {
            askWifiPermissions()
            listOf()
        }
        return Gson().toJson(networks)
    }

    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == WIFI_PERMISSIONS_CODE) {
            if (WIFI_PERMISSIONS.containsAll(permissions.toList())
                && grantResults.isNotEmpty()) {
                enableWifiIfRequired()
            } else {
                enablingWifiState = EnablingWifiState.NONE
            }
        }
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

    private fun hasWifiPermissions(): Boolean {
        val hasFineLocationPermission = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val hasCoarseLocationPermission = hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        return hasCoarseLocationPermission && hasFineLocationPermission
    }

    private fun askWifiPermissions() {
        if (enablingWifiState == EnablingWifiState.GRANTING_PERMISSIONS)
            return
        activityWeakReference.get()?.let { activity ->
            enablingWifiState = EnablingWifiState.GRANTING_PERMISSIONS
            ActivityCompat.requestPermissions(
                activity,
                WIFI_PERMISSIONS.toTypedArray(),
                WIFI_PERMISSIONS_CODE
            )
        }
    }

    private fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    private fun enableWifiIfRequired() {
        if (!wifiManager.isWifiEnabled)
            enableWifi()
        else
            enablingWifiState = EnablingWifiState.NONE
    }

    private fun enableWifi() {
        if (enablingWifiState == EnablingWifiState.ENABLING)
            return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            enableWifiAndroidQ()
        else
            enableWifiLegacy()
    }

    @Suppress("DEPRECATION")
    private fun enableWifiLegacy() {
        wifiManager.isWifiEnabled = true
    }

    private val wifiManager: WifiManager
        get() = context.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun enableWifiAndroidQ() {
        activityWeakReference.get()?.let { activity ->
            enablingWifiState = EnablingWifiState.ENABLING
            val panelIntent = Intent(Settings.Panel.ACTION_WIFI)
            startActivityForResult(activity, panelIntent, WIFI_PERMISSIONS_CODE, bundleOf())
        }
    }


    companion object {
        private val WIFI_PERMISSIONS = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        private const val WIFI_PERMISSIONS_CODE = 10
    }
}