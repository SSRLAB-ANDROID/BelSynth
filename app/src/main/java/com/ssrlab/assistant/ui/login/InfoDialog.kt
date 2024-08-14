package com.ssrlab.assistant.ui.login

import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ssrlab.assistant.R

fun createInfoDialog(
    context: Context,
    iconResId: Int,
    message: String,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_info, null)

    dialogView.background = ContextCompat.getDrawable(context, R.drawable.background_dialog)

    val dialog = MaterialAlertDialogBuilder(context)
        .setView(dialogView)
        .create()

    dialogView.findViewById<TextView>(R.id.dialog_message).text = message
    dialogView.findViewById<ImageView>(R.id.dialog_icon).setImageResource(iconResId)
    dialogView.findViewById<Button>(R.id.dialog_button).apply {
        text = buttonText
        setOnClickListener {
            onButtonClick()
            dialog.dismiss()
        }
    }

    dialog.show()
}