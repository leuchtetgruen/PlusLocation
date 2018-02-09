package de.leuchtetgruen.pluslocation.businessobjects

fun Double.toStandardDegrees() : Double = (this + 360) % 360