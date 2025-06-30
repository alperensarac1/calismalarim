package com.example.isletimsistemlericalismasikotlin.uygulamalar.qrokuyucu

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.isletimsistemlericalismasikotlin.R
import com.example.isletimsistemlericalismasikotlin.databinding.FragmentQrOkuyucuBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class QrOkuyucuFragment : Fragment() {

    private lateinit var binding: FragmentQrOkuyucuBinding
    private lateinit var kopyalamaPanosu: ClipboardManager
    private var clipData: ClipData? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentQrOkuyucuBinding.inflate(inflater, container, false)

        binding.btnQRTara.setOnClickListener {
            scanCode()
        }

        kopyalamaPanosu = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        binding.webView.settings.javaScriptEnabled = true
        binding.btnSiteAc.setOnClickListener {
            clipData?.getItemAt(0)?.text?.toString()?.let { kopyalanmisYazi ->
                binding.webView.loadUrl(kopyalanmisYazi)
            }
        }

        return binding.root
    }

    private fun scanCode() {
        val options = ScanOptions().apply {
            setPrompt("Flash açmak için ses arttırma tuşuna basın.")
            setBeepEnabled(true)
            setOrientationLocked(true)
            setCaptureActivity(CaptureAct::class.java)
        }
        barLauncher.launch(options)
    }

    private val barLauncher = registerForActivityResult(ScanContract()) { result ->
        result.contents?.let { content ->
            AlertDialog.Builder(requireContext()).apply {
                setTitle("Sonuç")
                setMessage(content)
                setPositiveButton("Kopyala") { dialog, _ ->
                    clipData = ClipData.newPlainText("text", content)
                    kopyalamaPanosu.setPrimaryClip(clipData!!)
                    Toast.makeText(requireContext(), "Panoya Kopyalandı", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                setNegativeButton("İptal Et") { dialog, _ ->
                    dialog.dismiss()
                }
                show()
            }
        }
    }
}
