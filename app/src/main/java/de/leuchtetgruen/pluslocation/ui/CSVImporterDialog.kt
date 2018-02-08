package de.leuchtetgruen.pluslocation.ui

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import de.leuchtetgruen.pluslocation.persistence.CSVImporter
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader

class CSVImporterDialog(act: Activity, reader : Reader) : ProgressDialog(act) {
    val csvImporter = CSVImporter(reader, act)

    companion object {
        fun startFromIntent(intent : Intent, act: Activity) {
            val uri = intent.data
            val inputStream = act.contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream))

            val dialog = CSVImporterDialog(act, reader)
            dialog.start()

        }
    }

    init {
        setTitle("Importing POIs")
        setProgressStyle(ProgressDialog.STYLE_SPINNER)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }

    fun start() {
        show()
        setMessage("Reading CSV...")
        csvImporter.import({
            setMessage(String.format("%d items imported... (%d errors)", it, csvImporter.getErrorCount()))
        }, {
            setMessage("Done")
            dismiss()
        })
    }
}