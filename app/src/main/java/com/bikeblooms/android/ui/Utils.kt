package com.bikeblooms.android.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.DatePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

    fun showDatePicker(
        context: Context, calendar: Calendar, listener: (calendar: Calendar) -> Unit
    ) {
        DatePickerDialog(
            context,
            0,
            object : DatePickerDialog.OnDateSetListener {
                override fun onDateSet(
                    picker: DatePicker?, year: Int, month: Int, day: Int
                ) {
                    listener(Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, day)
                    })
                }

            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = calendar.timeInMillis
            datePicker.maxDate = calendar.apply { add(Calendar.DAY_OF_MONTH, 7) }.timeInMillis
        }.show()
    }

    fun callPhone(context: Context, mobileNum: String) {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$mobileNum"));
        context.startActivity(intent);
    }

    fun openWhatsapp(context: Context, mobileNum: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("http://api.whatsapp.com/send?phone=$mobileNum")
        context.startActivity(intent)
    }

    fun sendEmail(context: Context, email: String) {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
        }
        context.startActivity(Intent.createChooser(emailIntent, "Send feedback"))
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

fun Date.toDisplayDate(): String {
    val df = SimpleDateFormat("DD-MM-yy", Locale.US);
    return df.format(this).toString()
}