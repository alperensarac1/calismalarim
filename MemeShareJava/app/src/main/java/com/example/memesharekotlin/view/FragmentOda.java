package com.example.memesharekotlin.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.memesharekotlin.R;
import com.example.memesharekotlin.adapter.GonderiAdapter;
import com.example.memesharekotlin.databinding.FragmentOdaBinding;
import com.example.memesharekotlin.model.GonderiModel;
import com.example.memesharekotlin.service.ApiClient;
import com.example.memesharekotlin.util.VideoUploader;
import com.example.memesharekotlin.viewmodel.OdaViewModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentOda extends Fragment {

    FragmentOdaBinding binding;
    private static final int REQUEST_CODE_MEDIA = 101;
    private Uri selectedUri;
    private boolean isVideo = false;
    int roomId, userId;

    private OdaViewModel odaViewModel;
    private GonderiAdapter gonderiAdapter;
    private List<GonderiModel> gonderiList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentOdaArgs args = FragmentOdaArgs.fromBundle(getArguments());
        roomId = args.getRoomId();
        userId = args.getUserId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOdaBinding.inflate(inflater, container, false);
        odaViewModel = new ViewModelProvider(requireActivity()).get(OdaViewModel.class);

        setupRecyclerView();
        loadMediaList();

        odaViewModel.uploadResult.observe(getViewLifecycleOwner(), mesaj -> {
            if (mesaj != null && (mesaj.contains("yüklendi") || mesaj.contains("yükleme hatası"))) {
                Toast.makeText(requireContext(), mesaj, Toast.LENGTH_SHORT).show();
                loadMediaList();
                gonderiAdapter.notifyDataSetChanged();
            }
        });

        binding.btnGonderiPaylas.setOnClickListener(view -> openGallery());

        return binding.getRoot();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        String[] mimeTypes = {"image/*", "video/*"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, REQUEST_CODE_MEDIA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_MEDIA && resultCode == Activity.RESULT_OK && data != null) {
            selectedUri = data.getData();

            if (selectedUri != null) {
                requireContext().getContentResolver().takePersistableUriPermission(
                        selectedUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                );

                // MIME type kontrolü burada yapılmalı:
                String mimeType = requireContext().getContentResolver().getType(selectedUri);
                isVideo = mimeType != null && mimeType.startsWith("video");

                Log.d("FragmentOda", "Seçilen MIME türü: " + mimeType);
            }

            Log.d("FragmentOda", "Seçilen URI: " + selectedUri);
            showPaylasDialogWithMedia(selectedUri, isVideo);
        }
    }


    private void showPaylasDialogWithMedia(Uri uri, boolean isVideoSelected) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_paylasim, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        Button btnGonder = dialogView.findViewById(R.id.btnGonder);
        EditText editCaption = dialogView.findViewById(R.id.editCaption);
        ImageView imagePreview = dialogView.findViewById(R.id.imagePreview);
        VideoView videoPreview = dialogView.findViewById(R.id.videoPreview);

        imagePreview.setVisibility(View.GONE);
        videoPreview.setVisibility(View.GONE);

        if (uri != null) {
            if (isVideoSelected) {
                imagePreview.setVisibility(View.VISIBLE);
                try {
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(requireContext(), uri);
                    Bitmap thumb = retriever.getFrameAtTime(1_000_000); // 1 saniye
                    imagePreview.setImageBitmap(thumb);
                    retriever.release();
                } catch (Exception e) {
                    Log.e("FragmentOda", "Thumbnail alınamadı: " + e.getMessage());
                    Toast.makeText(getContext(), "Video önizleme gösterilemedi", Toast.LENGTH_SHORT).show();
                }
            } else {
                imagePreview.setVisibility(View.VISIBLE);
                imagePreview.setImageURI(uri);
            }
        }

        btnGonder.setOnClickListener(v -> {
            String caption = editCaption.getText().toString().trim();

            if (uri != null) {
                if (isVideoSelected) {
                    VideoUploader.uploadVideo(
                            UUID.randomUUID().toString(),
                            uri,
                            requireActivity(),
                            roomId,
                            userId,
                            caption,
                            "https://alperensaracdeneme.com/meme/media-upload-video.php"
                    );
                } else {
                    odaViewModel.uploadImage(uri, roomId, userId, caption);
                }
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Medya seçimi yapılmadı", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void loadMediaList() {
        ApiClient.getService().getAllMedia(roomId).enqueue(new Callback<List<GonderiModel>>() {
            @Override
            public void onResponse(Call<List<GonderiModel>> call, Response<List<GonderiModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    gonderiList.clear();
                    gonderiList.addAll(response.body());
                    gonderiAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Sunucu hatası", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<GonderiModel>> call, Throwable t) {
                Toast.makeText(getContext(), "Bağlantı hatası: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        gonderiAdapter = new GonderiAdapter(requireContext(), gonderiList, userId);
        binding.rvGonderiler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvGonderiler.setHasFixedSize(true);
        binding.rvGonderiler.setAdapter(gonderiAdapter);
    }
}
