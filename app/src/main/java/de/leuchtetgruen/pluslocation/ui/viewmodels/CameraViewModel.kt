package de.leuchtetgruen.pluslocation.ui.viewmodels

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.hardware.Camera
import de.leuchtetgruen.pluslocation.businessobjects.toStandardDegrees
import de.leuchtetgruen.pluslocation.ui.CameraOverlayData
import de.leuchtetgruen.pluslocation.ui.activities.CameraActivity
import kotlin.math.absoluteValue

class CameraViewModel(private val app: Application?) : LocationHeadingViewModel(app) {

    companion object {
        fun create(activity: CameraActivity, cam : Camera): CameraViewModel {
            val vm = ViewModelProviders.of(activity).get(CameraViewModel::class.java)
            vm.setCameraAngle(cam.parameters.horizontalViewAngle)
            return vm
        }

    }

    val cameraOverlayDataLeft: MutableLiveData<CameraOverlayData?> = MutableLiveData()
    val cameraOverlayDataHalfLeft: MutableLiveData<CameraOverlayData?> = MutableLiveData()
    val cameraOverlayDataAheadBehind: MutableLiveData<CameraOverlayData?> = MutableLiveData()
    val cameraOverlayDataHalfRight: MutableLiveData<CameraOverlayData?> = MutableLiveData()
    val cameraOverlayDataRight: MutableLiveData<CameraOverlayData?> = MutableLiveData()

    private var totalVisibleAngle: Float = 0.0f

    private var minBearing = 0.0
    private var maxBearing = 0.0
    private var currentBearing = 0.0

    private var bearingDiff = 0.0

    private val labels = arrayOf(cameraOverlayDataLeft, cameraOverlayDataHalfLeft, cameraOverlayDataAheadBehind, cameraOverlayDataHalfRight, cameraOverlayDataRight)


    override fun headingChanged(heading: Double) {
        minBearing = (heading - (totalVisibleAngle / 2)).toStandardDegrees()
        maxBearing = (heading + (totalVisibleAngle / 2)).toStandardDegrees()
        currentBearing = heading.toStandardDegrees()

        calculateBearingDiff()
        updateLabels()
    }

    private fun calculateBearingDiff() {
        if (bearingToTarget == null) return

        val diff = bearingToTarget!! - currentBearing


        this.bearingDiff = if ((-180 < diff) && (diff < 180)) {
            diff
        }
        else if (diff > 180) {
            diff - 360
        }
        else  {
            //(d < -180)
            diff + 360
        }
    }

    private fun updateLabels() {
        clearLabels()

        val label = findRightLabel()
        val text = buildText()

        if (label != null) {
            label.value = CameraOverlayData(text, Color.RED, distanceToTargetInMeter!!)
        }
    }


    private fun findRightLabel(): MutableLiveData<CameraOverlayData?>? {
        if (bearingToTarget == null) return null


        // out of sight
        if ((bearingToTarget!! < minBearing) || (bearingToTarget!! > maxBearing)) {
            // Behind me
            if (bearingDiff.absoluteValue > 145) return cameraOverlayDataAheadBehind

            // to the left or to the right of me
            return if (bearingDiff < 0) cameraOverlayDataLeft
            else cameraOverlayDataRight
        }

        val idx = (((bearingToTarget!! - minBearing) / totalVisibleAngle) * labels.size).toInt()
        return labels[idx]
    }

    private fun buildText(): String {
        if (bearingToTarget == null) return ""

        if ((bearingToTarget!! >= minBearing) && (bearingToTarget!! <= maxBearing)) {
            return targetName!!.value!!
        }

        return when {
            bearingDiff < -145 -> "↓↓↓"
            bearingDiff < -112 -> "<<<"
            bearingDiff < -66 -> "<<"
            bearingDiff < 0 -> "<"
            bearingDiff > 145 -> "↓↓↓"
            bearingDiff > 112 -> ">>>"
            bearingDiff > 66 -> ">>"
            bearingDiff > 0 -> ">"
            else -> ""
        }

    }

    private fun clearLabels() {
        labels.forEach {
            it.value = null
        }

    }

    private fun setCameraAngle(cameraAngle : Float) {
        this.totalVisibleAngle = cameraAngle
    }

}