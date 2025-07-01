package com.example.adisyonuygulamajava.masadetay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.adisyonuygulamajava.model.Masa;
import com.example.adisyonuygulamajava.viewmodel.MasaDetayViewModel;
import com.example.adisyonuygulamajava.viewmodel.MasalarViewModel;
import com.example.adisyonuygulamajava.viewmodel.UrunViewModel;

public class MasaDetayFragment extends Fragment {

    private MasaDetayViewModel viewModel;
    private UrunViewModel urunViewModel;

    private MasaDetayLayout layout;
    private LinearLayout containerLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        containerLayout = new LinearLayout(requireContext());
        containerLayout.setOrientation(LinearLayout.VERTICAL);
        containerLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        urunViewModel = new ViewModelProvider(this).get(UrunViewModel.class);
        urunViewModel.kategorileriYukle();

        return containerLayout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int masaId = getArguments() != null ? getArguments().getInt("masa_id", -1) : -1;

        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            @SuppressWarnings("unchecked")
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new MasaDetayViewModel(masaId);
            }
        }).get(MasaDetayViewModel.class);

        Masa masa = new Masa(masaId, "", 1, "00:00", 0f);

        layout = new MasaDetayLayout(
                requireContext(),
                masa,
                viewModel,
                urunViewModel
        );

        containerLayout.addView(layout.show());

        urunViewModel.urunleriYukle();

        urunViewModel.getUrunler().observe(getViewLifecycleOwner(), urunler -> {
            layout.urunlerLayout.setUrunListesi(urunler);
        });

        urunViewModel.getKategoriler().observe(getViewLifecycleOwner(), kategoriler -> {
            layout.urunlerLayout.setKategoriListesi(kategoriler);
        });

        viewModel.getToplamFiyat().observe(getViewLifecycleOwner(), fiyat -> {
            layout.masaAdisyonLayout.guncelleToplamFiyat(fiyat);
        });

        viewModel.odemeTamamlandi.observe(getViewLifecycleOwner(), tamamlandi -> {
            if (Boolean.TRUE.equals(tamamlandi)) {
                Toast.makeText(requireContext(), "Ödeme tamamlandı", Toast.LENGTH_SHORT).show();

                // Verileri güncelle
                viewModel.yukleTumVeriler();

                // Güncellenmiş masayı al
                Masa guncelMasa = viewModel.getMasa().getValue();
                if (guncelMasa != null) {
                    MasalarViewModel activityViewModel = new ViewModelProvider(requireActivity())
                            .get(MasalarViewModel.class);
                    activityViewModel.guncelleMasa(guncelMasa);
                }

                // Fragment geri git
                requireActivity().getSupportFragmentManager().popBackStack();

                // Bir daha tetiklenmesini önle
                viewModel.odemeTamamlandi.setValue(false);
            }
        });


        viewModel.yukleTumVeriler();
    }

    public static MasaDetayFragment newInstance(int masaId) {
        MasaDetayFragment fragment = new MasaDetayFragment();
        Bundle args = new Bundle();
        args.putInt("masa_id", masaId);
        fragment.setArguments(args);
        return fragment;
    }
}
