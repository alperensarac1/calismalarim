package com.example.isletimsistemicalismasi.uygulamalar.galeri;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.isletimsistemicalismasi.R;
import com.example.isletimsistemicalismasi.databinding.FragmentGaleriBinding;


public class GaleriFragment extends Fragment {
    ActivityResultLauncher<Intent> resultLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    FragmentGaleriBinding binding;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        binding  = FragmentGaleriBinding.inflate(inflater,container,false);
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), this::handleGalleryResult);

        binding.button.setOnClickListener(view->{
            galleryLauncher.launch("image/*");

        });

        return binding.getRoot();
    }

    private void pickImage(){
        Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        startActivityForResult(intent,1);
    }

    private void handleGalleryResult(Uri galleryUri) {
        if (galleryUri != null) {
            try {
                binding.image.setImageURI(galleryUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}