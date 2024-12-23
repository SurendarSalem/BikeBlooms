package com.bikeblooms.android.ui

import android.app.AlertDialog
import android.content.Context

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
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, positiveBtnText) { _, _ ->
            alertDialog.dismiss()
            positiveBtnCallback()
        }
        negativeBtnText?.let {
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, negativeBtnText) { _, _ ->
                alertDialog.dismiss();
                negativeBtnCallback()
            }
        }
        alertDialog.show();
    }
}