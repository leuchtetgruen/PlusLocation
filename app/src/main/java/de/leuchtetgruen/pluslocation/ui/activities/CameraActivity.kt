package de.leuchtetgruen.pluslocation.ui.activities

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.hardware.Camera
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import de.leuchtetgruen.pluslocation.R
import de.leuchtetgruen.pluslocation.ui.CameraOverlayData
import de.leuchtetgruen.pluslocation.ui.viewmodels.CameraViewModel
import kotlinx.android.synthetic.main.activity_camera.*
import java.lang.Double.max

class CameraActivity : LocationActivity() {



    companion object {
        fun intentTo(context : Context) : Intent = Intent(context, CameraActivity::class.java)
    }

    // Observers
    private var leftObserver : android.arch.lifecycle.Observer<CameraOverlayData?> = Observer { updateLabel(it, labelLeft) }
    private var halfLeftObserver : android.arch.lifecycle.Observer<CameraOverlayData?> = Observer { updateLabel(it, labelHalfLeft) }
    private var straightObserver : android.arch.lifecycle.Observer<CameraOverlayData?> = Observer { updateLabel(it, labelStraightAhead) }
    private var halfRightObserver : android.arch.lifecycle.Observer<CameraOverlayData?> = Observer { updateLabel(it, labelHalfRight) }
    private var rightObserver : android.arch.lifecycle.Observer<CameraOverlayData?> = Observer { updateLabel(it, labelRight) }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        viewModel = CameraViewModel.create(this, Camera.open())
    }

    override fun onResume() {
        super.onResume()
        cameraView.start()
    }

    override fun onPause() {
        cameraView.stop()
        super.onPause()
    }

    override fun onStart() {
        super.onStart()
        addObservers()
    }

    override fun onStop() {
        removeObservers()
        super.onStop()
    }

    private fun updateLabel(it: CameraOverlayData?, label: TextView?) {
        if (it == null) {
            label?.text = ""
            setLabelWeight(label, 1)
            return
        }

        label?.text = it.text + "\n\n" + String.format("%.1fkm", (it.distanceInMeters / 1000))
        label?.setTextColor(it.color)

        label?.textSize = max(36 - (it.distanceInMeters / 250), 14.0).toFloat()
        setLabelWeight(label, 3)
    }

    private fun setLabelWeight(label: TextView?, weight: Int) {
        if (label == null) return
        val params = label!!.layoutParams as LinearLayout.LayoutParams
        params.weight = weight.toFloat()

        label.layoutParams = params
    }

    private fun addObservers() {
        val vm = viewModel as CameraViewModel

        vm.cameraOverlayDataLeft.observe(this, leftObserver)
        vm.cameraOverlayDataHalfLeft.observe(this, halfLeftObserver)
        vm.cameraOverlayDataAheadBehind.observe(this, straightObserver)
        vm.cameraOverlayDataHalfRight.observe(this, halfRightObserver)
        vm.cameraOverlayDataRight.observe(this, rightObserver)

        lifecycle.addObserver(vm)
    }

    private fun removeObservers() {
        val vm = viewModel as CameraViewModel

        vm.cameraOverlayDataLeft.removeObserver(leftObserver)
        vm.cameraOverlayDataHalfLeft.removeObserver(halfLeftObserver)
        vm.cameraOverlayDataAheadBehind.removeObserver(straightObserver)
        vm.cameraOverlayDataHalfRight.removeObserver(halfRightObserver)
        vm.cameraOverlayDataRight.removeObserver(rightObserver)
    }

}


