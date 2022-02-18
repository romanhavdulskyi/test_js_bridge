package com.havdulskyi.test_js_bridge

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class QrContract : ActivityResultContract<QrContract.NoParam, QrContract.Action>() {

    override fun createIntent(context: Context, input: NoParam): Intent {
        return Intent(context, QrScannerActivity::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Action {
        return if(resultCode == QR_SCANNED_SUCCESSFULLY) {
            val result = intent?.getStringExtra(QR_RESULT_KEY)
            result?.let { Action.QrResult(it) } ?: Action.NothingAction
        } else {
            Action.NothingAction
        }
    }

    object NoParam

    sealed class Action {
        data class QrResult(val data : String) : Action()
        object NothingAction : Action()
    }

    companion object {
        const val QR_RESULT_KEY = "qr_result"
        const val QR_SCANNED_SUCCESSFULLY = 101
        const val QR_SCANNING_FAILED = 102
    }
}