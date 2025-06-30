package com.example.isletimsistemlericalismasikotlin.uygulamalar.galeri

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.isletimsistemlericalismasikotlin.R
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import com.example.isletimsistemlericalismasikotlin.databinding.FragmentGaleriBinding


class GaleriFragment : Fragment() {
    private lateinit var binding: FragmentGaleriBinding
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        handleGalleryResult(uri)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentGaleriBinding.inflate(inflater, container, false)

        binding.button.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        return binding.root
    }

    private fun pickImage() {
        val intent = Intent(MediaStore.ACTION_PICK_IMAGES)
        startActivityForResult(intent, 1)
    }

    private fun handleGalleryResult(galleryUri: Uri?) {
        galleryUri?.let {
            try {
                binding.image.setImageURI(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
