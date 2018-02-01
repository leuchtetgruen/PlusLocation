package de.leuchtetgruen.pluslocation.helpers

import android.content.Context
import de.leuchtetgruen.pluslocation.R
import de.leuchtetgruen.pluslocation.persistence.POIDatabase
import java.io.InputStreamReader

object InitialImporter {

    fun import(ctx : Context) {
        POIDatabase.build(ctx)
        if (POIDatabase.dao().count() > 0) return

        val ins = ctx.resources.openRawResource(R.raw.berlin_nearby)
        val ir = InputStreamReader(ins)
        POIDatabase.importFromCSV(ir)
    }
}