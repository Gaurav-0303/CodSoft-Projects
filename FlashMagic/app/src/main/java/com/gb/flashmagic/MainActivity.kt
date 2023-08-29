package com.gb.flashmagic

import android.app.Service
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import android.widget.ToggleButton
import com.google.android.material.switchmaterial.SwitchMaterial

class MainActivity : AppCompatActivity() {

    private lateinit var imageButton: ImageButton
    private lateinit var toggleButton: SwitchMaterial
    private val camManager: CameraManager by lazy { getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    private lateinit var cameraId: String
    private var torchStatus: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageButton = findViewById(R.id.image_button_on_off)
        toggleButton = findViewById(R.id.toggle_button)

        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                turnOnFlash()
            } else {
                turnOffFlash()
            }
        }

        imageButton.setOnClickListener {
            if(!torchStatus) turnOnFlash()
            else if(torchStatus) turnOffFlash()
        }
    }

    private fun turnOnFlash() {
        if (hasFlashlight()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    cameraId = camManager.cameraIdList[0]
                    camManager.setTorchMode(cameraId, true) // Turn ON
                    imageButton.setImageResource(R.drawable.bulb_on)
                    torchStatus = true
                } catch (e: CameraAccessException) {
                    e.printStackTrace()
                }
            }
        } else {
            Toast.makeText(this, "No Flashlight Available", Toast.LENGTH_LONG).show()
        }
    }

    private fun turnOffFlash() {
        camManager.setTorchMode(cameraId, false);
        imageButton.setImageResource(R.drawable.bulb_off_same)
        toggleButton.isChecked = false
        torchStatus = false
    }

    private fun hasFlashlight(): Boolean {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }

    override fun onPause() {
        super.onPause()
        turnOffFlash()
    }

}

