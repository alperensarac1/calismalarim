package com.example.isletimsistemicalismasi.uygulamalar.mesajlasma;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.isletimsistemicalismasi.R;
import com.example.isletimsistemicalismasi.databinding.FragmentMesajlasmaAnasayfaBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;


public class MesajlasmaAnasayfa extends Fragment {

    private static int PERMISSION_REQUEST_SEND_SMS = 105;
   FragmentMesajlasmaAnasayfaBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMesajlasmaAnasayfaBinding.inflate(inflater,container,false);

        sendSMS();
        binding.btnMesajGonder.setOnClickListener(view->{
            String mesaj = binding.etTelefonMesaj.getText().toString();
            String gonderilecekNumara = binding.etTelefonNumara.getText().toString();
           /* Intent smsSayfasinaGecis = new Intent(Intent.ACTION_SENDTO, Uri.parse("sms:"+gonderilecekNumara));
            smsSayfasinaGecis.putExtra("sms_body",mesaj);
            startActivity(smsSayfasinaGecis);*/
            SmsManager sms = SmsManager.getDefault();
            ArrayList<String> ist = sms.divideMessage(mesaj);
            sms.sendMultipartTextMessage(gonderilecekNumara,null,ist,null,null);
            binding.etTelefonMesaj.setText("");
            binding.etTelefonNumara.setText("");
            Snackbar.make(view,"Mesaj Gönderildi",Snackbar.LENGTH_SHORT).show();
        });
        return binding.getRoot();
    }
    protected void sendSMS() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.SEND_SMS},
                        PERMISSION_REQUEST_SEND_SMS);
            }
        }
    }
}