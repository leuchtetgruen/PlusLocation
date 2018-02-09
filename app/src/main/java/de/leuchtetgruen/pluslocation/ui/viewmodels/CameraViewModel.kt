package de.leuchtetgruen.pluslocation.ui.viewmodels

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.hardware.Camera
import de.leuchtetgruen.pluslocation.ui.LabelData
import de.leuchtetgruen.pluslocation.ui.activities.CameraActivity

class CameraViewModel(private val app: Application?) : LocationHeadingViewModel(app) {

    companion object {
        fun create(activity: CameraActivity, cam : Camera): CameraViewModel {
            val vm = ViewModelProviders.of(activity).get(CameraViewModel::class.java)
            vm.setCameraAngle(cam.parameters.horizontalViewAngle)
            return vm
        }
    }

    val labelDataLeft : MutableLiveData<LabelData> = MutableLiveData()
    val labelDataHalfLeft : MutableLiveData<LabelData> = MutableLiveData()
    val labelDataAhead : MutableLiveData<LabelData> = MutableLiveData()
    val labelDataHalfRight : MutableLiveData<LabelData> = MutableLiveData()
    val labelDataRight : MutableLiveData<LabelData> = MutableLiveData()

    private var cameraAngle : Float = 0.0f


    override fun headingChanged(heading: Float) {
        if ((currentCoordinate == null) || (targetCoordinate == null)) return

        val desiredHeading = currentCoordinate!!.bearingInDegrees(targetCoordinate!!)

        if (angleInView(heading, desiredHeading.toFloat())) {
            labelDataAhead.value = LabelData("TARGET", Color.RED)
        }
        else {
            labelDataAhead.value = LabelData("", Color.TRANSPARENT)
        }
    }

    private fun setCameraAngle(cameraAngle : Float) {
        this.cameraAngle = cameraAngle
    }

    private fun angleInView(currentHeading: Float, desiredHeading : Float) : Boolean {
        val halfCameraAngle = cameraAngle / 2

        return (((currentHeading - halfCameraAngle) <= desiredHeading) &&
                ((currentHeading + halfCameraAngle) >= desiredHeading))
    }
}