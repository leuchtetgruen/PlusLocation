package de.leuchtetgruen.pluslocation.helpers

import android.content.Context
import de.leuchtetgruen.pluslocation.R
import de.leuchtetgruen.pluslocation.persistence.POIDatabase
import java.io.InputStreamReader
import java.io.Reader

object InitialImporter {

    fun import(ctx : Context) {
        POIDatabase.build(ctx)
        val count = POIDatabase.dao().count()
        if (count > 0) return

        val ins = ctx.resources.openRawResource(R.raw.berlin)
        val ir = InputStreamReader(ins)
        POIDatabase.importFromCSV(ir as Reader)
    }
}