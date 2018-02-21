package de.leuchtetgruen.pluslocation.helpers.ui

import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity

open class PermissionActivity : AppCompatActivity() {

    private var listener: PermissionListener? = null
    private var requestCode: Int = 0

    interface PermissionListener {
        fun permissionGranted(permission : String)
        fun permissionNotGranted()
    }


    fun requestPermission(permission: String, listener: PermissionListener) {
        this.listener = listener

        requestCode = (Math.random() * 16000).toInt()


        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            }
        } else {
            listener.permissionGranted(permission)
        }
    }

    override fun onRequestPermissionsResult(returnedRequestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (returnedRequestCode == requestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissions.forEach {
                    listener!!.permissionGranted(it)
                }

            } else {
                listener!!.permissionNotGranted()
            }
        }
    }
}