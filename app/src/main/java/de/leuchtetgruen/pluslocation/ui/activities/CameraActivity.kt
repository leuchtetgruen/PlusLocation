package de.leuchtetgruen.pluslocation.ui.activities

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.hardware.Camera
import android.os.Bundle
import android.widget.TextView
import de.leuchtetgruen.pluslocation.R
import de.leuchtetgruen.pluslocation.ui.LabelData
import de.leuchtetgruen.pluslocation.ui.viewmodels.CameraViewModel
import kotlinx.android.synthetic.main.activity_camera.*

class CameraActivity : LocationActivity() {



    companion object {
        fun intentTo(context : Context) : Intent = Intent(context, CameraActivity::class.java)
    }

    // Observers
    private var straightObserver : android.arch.lifecycle.Observer<LabelData> = Observer { updateLabel(it!!, labelStraightAhead) }




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

    private fun updateLabel(it: LabelData, label: TextView?) {
        label?.text = it.text
        label?.setTextColor(it.color)
    }

    private fun addObservers() {
        val vm = viewModel as CameraViewModel

        vm.labelDataAhead.observe(this, straightObserver)

        lifecycle.addObserver(vm)
    }

    private fun removeObservers() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}


