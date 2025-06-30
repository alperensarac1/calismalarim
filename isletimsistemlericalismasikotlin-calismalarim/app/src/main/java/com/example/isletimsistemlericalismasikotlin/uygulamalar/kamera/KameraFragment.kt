package com.example.isletimsistemlericalismasikotlin.uygulamalar.kamera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.isletimsistemlericalismasikotlin.R
import com.example.isletimsistemlericalismasikotlin.databinding.FragmentKameraBinding

class KameraFragment : Fragment() {

    private val picId = 123
    private val cameraPermissionRequestCode = 100
    private lateinit var binding: FragmentKameraBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentKameraBinding.inflate(inflater, container, false)

        if (ContextCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.CAMERA), cameraPermissionRequestCode
            )
        }

        binding.cameraButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireActivity(), Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, picId)
            } else {
                Toast.makeText(requireActivity(), "Kamera izni verilmedi!", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == cameraPermissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireActivity(), "Kamera izni verildi", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireActivity(), "Kamera izni gereklidir", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == picId && resultCode == Activity.RESULT_OK && data != null) {
            val photo = data.extras?.get("data") as? Bitmap
            binding.clickImage.setImageBitmap(photo)
        }
    }
}
