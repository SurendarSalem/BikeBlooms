package com.bikeblooms.android.ui

import android.app.AlertDialog
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Utils {
    fun showAlertDialog(
        context: Context,
        message: String,
        positiveBtnText: String,
        positiveBtnCallback: () -> Unit,
        negativeBtnText: String? = null,
        negativeBtnCallback: () -> Unit? = {}
    ) {
        var alertDialog = AlertDialog.Builder(context).create();
        alertDialog.setMessage(message)
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, positiveBtnText) { _, _ ->
            alertDialog.dismiss()
            positiveBtnCallback()
        }
        negativeBtnText?.let {
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, negativeBtnText) { _, _ ->
                alertDialog.dismiss()
                negativeBtnCallback()
            }
        }
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isAllCaps = false
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).isAllCaps = false
    }
}

fun CoroutineScope.inMainThread(block: suspend CoroutineScope.() -> Unit) {
    launch(Dispatchers.Main) {
        block()
    }
}

fun CoroutineScope.inIOThread(block: suspend CoroutineScope.() -> Unit) {
    launch(Dispatchers.IO) {
        block()
    }
}