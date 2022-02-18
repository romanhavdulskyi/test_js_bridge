package com.havdulskyi.test_js_bridge

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var codeScanner: CodeScanner

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

        codeScanner = CodeScanner(this, scannerView)
        setQrCallbacks()
        setListeners()
        Handler(Looper.getMainLooper()).postDelayed({
            val resultIntent = Intent().apply {
                putExtra(QR_RESULT_KEY, "temp qr data")
            }
            setResult(QR_SCANNED_SUCCESSFULLY, resultIntent)
            finish()
        }, 2500)
    }

    private fun setListeners() {
        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    private fun setQrCallbacks() {
        codeScanner.decodeCallback = qrDecodedCallback
        codeScanner.errorCallback = qrErrorCallback
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
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    companion object {
        const val EXTRA_QR_CONTENT_TEXT = "extra_qr_content_text"
        const val EXTRA_QR_CONTENT_RAW = "extra_qr_content_raw"
    }
}