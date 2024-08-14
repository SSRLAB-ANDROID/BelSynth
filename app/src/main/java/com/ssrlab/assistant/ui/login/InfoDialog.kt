package com.ssrlab.assistant.ui.login

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun createSimpleAlertDialog(message: String, buttonMessage: String, context: Context) {
    MaterialAlertDialogBuilder(context)
        .setMessage(message)
        .setPositiveButton(buttonMessage) { dialog, _ -> dialog.dismiss() }
        .setCancelable(false)
        .show()
}