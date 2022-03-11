package com.havdulskyi.test_js_bridge

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.google.zxing.Result
import com.havdulskyi.test_js_bridge.QrContract.Companion.QR_RESULT_KEY
import com.havdulskyi.test_js_bridge.QrContract.Companion.QR_SCANNED_SUCCESSFULLY
import com.havdulskyi.test_js_bridge.QrContract.Companion.QR_SCANNING_FAILED


class QrScannerActivity : AppCompatActivity() {

    private lateinit var scannerView: CodeScannerView
    private var codeScanner: CodeScanner? = null

    private val qrDecodedCallback = DecodeCallback { result ->
        runOnUiThread {
            returnQrResult(result)
            Toast.makeText(this@QrScannerActivity, result.text, Toast.LENGTH_SHORT).show()
        }
    }

    private val qrErrorCallback = ErrorCallback { error ->
        runOnUiThread {
            Toast.makeText(this@QrScannerActivity, error.message, Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scanner)
        scannerView = findViewById(R.id.qr_scanner)
        checkPermissionAndOpenCamera()
    }

    private fun setupQRScanner() {
        codeScanner = CodeScanner(this, scannerView)
        setQrCallbacks()
        setListeners()
        codeScanner?.startPreview()
    }

    private fun setListeners() {
        scannerView.setOnClickListener {
            codeScanner?.startPreview()
        }
    }

    private fun setQrCallbacks() {
        codeScanner?.decodeCallback = qrDecodedCallback
        codeScanner?.errorCallback = qrErrorCallback
    }


    private fun returnQrResult(result: Result) {
        val resultIntent = Intent().apply {
            putExtra(EXTRA_QR_CONTENT_RAW, result.rawBytes)
            putExtra(QR_RESULT_KEY, result.text)
        }
        setResult(QR_SCANNED_SUCCESSFULLY, resultIntent)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(QR_SCANNING_FAILED)
        finish()
    }

    override fun onResume() {
        super.onResume()
        codeScanner?.startPreview()
    }

    override fun onPause() {
        codeScanner?.releaseResources()
        super.onPause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupQRScanner()
            }
        }
    }

    /**
     * This will check your app camera permission.
     * If its granted, open camera
     * else request camera permission then open it later.
     */
    private fun checkPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CODE)
        } else {
            setupQRScanner()
        }
    }

    companion object {
        const val PERMISSION_REQUEST_CODE = 5
        const val EXTRA_QR_CONTENT_RAW = "extra_qr_content_raw"
    }
}