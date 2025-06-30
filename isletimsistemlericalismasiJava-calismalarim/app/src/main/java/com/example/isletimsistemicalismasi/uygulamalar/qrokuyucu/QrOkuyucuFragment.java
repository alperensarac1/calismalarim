package com.example.isletimsistemicalismasi.uygulamalar.qrokuyucu;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.isletimsistemicalismasi.R;
import com.example.isletimsistemicalismasi.databinding.FragmentQrOkuyucuBinding;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;


public class QrOkuyucuFragment extends Fragment {

    FragmentQrOkuyucuBinding binding;
    ClipboardManager kopyalamaPanosu;
    ClipData clipData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentQrOkuyucuBinding.inflate(inflater,container,false);
        binding.btnQRTara.setOnClickListener(view->{
            scanCode();
        });
        kopyalamaPanosu = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.btnSiteAc.setOnClickListener(view->{
            ClipData.Item item = clipData.getItemAt(0);
            String kopyalanmisYazi = item.getText().toString();
            binding.webView.loadUrl(kopyalanmisYazi);
        });

        return binding.getRoot();
    }


    private void scanCode(){
        ScanOptions options = new ScanOptions();
        options.setPrompt("Flash açmak için ses arttırma tuşuna basın.");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(),result->{
        if(result.getContents() != null){
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Sonuç");
            builder.setMessage(result.getContents());
            builder.setPositiveButton("Kopyala", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String kopyalanacakYazi = result.getContents();
                    clipData = ClipData.newPlainText("text",kopyalanacakYazi);
                    kopyalamaPanosu.setPrimaryClip(clipData);
                    Toast.makeText(requireContext(),"Panoya Kopyalandı",Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("İptal Et", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    });

}