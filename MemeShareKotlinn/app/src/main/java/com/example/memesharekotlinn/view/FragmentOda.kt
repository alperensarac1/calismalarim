package com.example.memesharekotlinn.view

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import android.widget.VideoView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memesharekotlinn.R
import com.example.memesharekotlinn.adapter.GonderiAdapter
import com.example.memesharekotlinn.databinding.FragmentOdaBinding
import com.example.memesharekotlinn.model.GonderiModel
import com.example.memesharekotlinn.service.ApiClient
import com.example.memesharekotlinn.util.VideoUploader
import com.example.memesharekotlinn.viewmodel.OdaViewModel
import java.util.UUID
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FragmentOda : Fragment() {

    private var _binding: FragmentOdaBinding? = null
    private val binding get() = _binding!!

    private val REQUEST_CODE_MEDIA = 101
    private var selectedUri: Uri? = null
    private var isVideo: Boolean = false

    private var roomId: Int = 0
    private var userId: Int = 0

    private lateinit var odaViewModel: OdaViewModel
    private lateinit var gonderiAdapter: GonderiAdapter
    private val gonderiList = mutableListOf<GonderiModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Safe Args
        val args = FragmentOdaArgs.fromBundle(requireArguments())
        roomId = args.roomId
        userId = args.userId
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOdaBinding.inflate(inflater, container, false)
        odaViewModel = ViewModelProvider(requireActivity())[OdaViewModel::class.java]

        setupRecyclerView()
        loadMediaList()

        odaViewModel.uploadResult.observe(viewLifecycleOwner) { mesaj ->
            if (mesaj != null && (mesaj.contains("yüklendi") || mesaj.contains("yükleme hatası"))) {
                Toast.makeText(requireContext(), mesaj, Toast.LENGTH_SHORT).show()
                loadMediaList()
                gonderiAdapter.notifyDataSetChanged()
            }
        }

        binding.btnGonderiPaylas.setOnClickListener { openGallery() }

        return binding.root
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
        }
        startActivityForResult(intent, REQUEST_CODE_MEDIA)
    }

    @Deprecated("startActivityForResult kullanımı için override")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_MEDIA && resultCode == Activity.RESULT_OK && data != null) {
            selectedUri = data.data

            selectedUri?.let { uri ->
                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                val mimeType = requireContext().contentResolver.getType(uri)
                isVideo = mimeType?.startsWith("video") == true
                Log.d("FragmentOda", "Seçilen MIME türü: $mimeType")
                Log.d("FragmentOda", "Seçilen URI: $uri")
                showPaylasDialogWithMedia(uri, isVideo)
            }
        }
    }

    private fun showPaylasDialogWithMedia(uri: Uri, isVideoSelected: Boolean) {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_paylasim, null)
        builder.setView(dialogView)
        val dialog = builder.create()

        val btnGonder: Button = dialogView.findViewById(R.id.btnGonder)
        val editCaption: EditText = dialogView.findViewById(R.id.editCaption)
        val imagePreview: ImageView = dialogView.findViewById(R.id.imagePreview)
        val videoPreview: VideoView = dialogView.findViewById(R.id.videoPreview)

        imagePreview.visibility = View.GONE
        videoPreview.visibility = View.GONE

        if (isVideoSelected) {
            // Video için thumbnail göster
            imagePreview.visibility = View.VISIBLE
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(requireContext(), uri)
                val thumb: Bitmap? = retriever.getFrameAtTime(1_000_000) // 1. saniye
                imagePreview.setImageBitmap(thumb)
                retriever.release()
            } catch (e: Exception) {
                Log.e("FragmentOda", "Thumbnail alınamadı: ${e.message}")
                Toast.makeText(context, "Video önizleme gösterilemedi", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Görsel için direkt göster
            imagePreview.visibility = View.VISIBLE
            imagePreview.setImageURI(uri)
        }

        btnGonder.setOnClickListener {
            val caption = editCaption.text.toString().trim()
            if (isVideoSelected) {
                VideoUploader.uploadVideo(
                    UUID.randomUUID().toString(),
                    uri,
                    requireActivity(),
                    roomId,
                    userId,
                    caption,
                    "https://alperensaracdeneme.com/meme/media-upload-video.php"
                )
            } else {
                odaViewModel.uploadImage(uri, roomId, userId, caption)
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun loadMediaList() {
        ApiClient.getService().getAllMedia(roomId)
            .enqueue(object : Callback<List<GonderiModel>> {
                override fun onResponse(
                    call: Call<List<GonderiModel>>,
                    response: Response<List<GonderiModel>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        gonderiList.clear()
                        gonderiList.addAll(response.body()!!)
                        gonderiAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(context, "Sunucu hatası", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<GonderiModel>>, t: Throwable) {
                    Toast.makeText(context, "Bağlantı hatası: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupRecyclerView() {
        gonderiAdapter = GonderiAdapter(requireContext(), gonderiList, userId)
        binding.rvGonderiler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = gonderiAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
