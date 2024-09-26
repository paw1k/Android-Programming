package com.my.intotal.Utils

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import dev.almasum.fittrack.R

class LoadingDialog : AppCompatDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(
            requireActivity(), R.style.DialogCustomTheme
        )
        val inflater = requireActivity().layoutInflater
        val view: View = inflater.inflate(R.layout.loading_dialog, null)
        builder.setView(view)
        return builder.create()
    }
}
