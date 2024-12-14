package com.bikeblooms.android.util

import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.EditText

import androidx.appcompat.widget.AppCompatEditText
import com.bikeblooms.android.R

class GenericTextWatcher(
    val currentView: AppCompatEditText, val nextView: AppCompatEditText?
) : TextWatcher {

    override fun afterTextChanged(editable: Editable) {

        val text = editable.toString()
        if (nextView != null && text.length == getMaxLength(currentView)) {
            nextView.requestFocus()
        }
        if (text.length > getMaxLength(currentView)) {
            currentView.setText(text[text.length - 1].toString())
            currentView.setSelection(1)
        }
    }

    private fun getMaxLength(currentView1: AppCompatEditText): Int {
        if (currentView.id == R.id.rn_4) {
            return 4;
        }
        return 2;
    }

    override fun beforeTextChanged(arg0: CharSequence?, arg1: Int, arg2: Int, arg3: Int) {

    }

    override fun onTextChanged(arg0: CharSequence?, arg1: Int, arg2: Int, arg3: Int) {

    }
}

class GenericKeyEvent(
    val currentView: EditText, val previousView: EditText?
) : View.OnKeyListener {

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL && currentView.getText()
                .toString().isEmpty()
        ) {
            previousView?.requestFocus()
            return true
        }
        return false
    }
}