package com.example.myapplication.services

import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.myapplication.R
import com.example.myapplication.util.RunConstants.ACTION_LOCATION_PERMISSION
import com.example.myapplication.util.RunConstants.EXTRA_PERMISSION
import com.example.myapplication.util.RunConstants.LOCATION_PERMISSION_REQUEST_CODE
import com.example.myapplication.util.RunConstants.hasLocationPerm
import com.example.myapplication.util.RunConstants.locationPermissions
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class PermissionsActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions)

        EasyPermissions.requestPermissions(this,"accept the permissions",
            LOCATION_PERMISSION_REQUEST_CODE,*locationPermissions)

//        findViewById<Button>(R.id.btn_click).setOnClickListener{
////            ActivityCompat.requestPermissions(this, locationPermissions, LOCATION_PERMISSION_REQUEST_CODE)
////            requestPermissions(locationPermissions, LOCATION_PERMISSION_REQUEST_CODE)
//
//            Log.d("PermissionsActivity", "onCreate: button clicked")
//        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if(requestCode== LOCATION_PERMISSION_REQUEST_CODE){
//            sendPermissionResult(
//                grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
//            )
//        }
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
//        finish()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if(requestCode== LOCATION_PERMISSION_REQUEST_CODE){
            sendPermissionResult(perms == locationPermissions)
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this,perms))
            AppSettingsDialog.Builder(this).build().show()
        else requestPerms()
    }

    private fun requestPerms(){
        if(hasLocationPerm()) return
        EasyPermissions.requestPermissions(this,"Enable these perms",
            LOCATION_PERMISSION_REQUEST_CODE,*locationPermissions)
    }

    private fun sendPermissionResult(granted: Boolean) {
        val intent = Intent(ACTION_LOCATION_PERMISSION)
        intent.putExtra(EXTRA_PERMISSION, granted)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        finish()
    }
}