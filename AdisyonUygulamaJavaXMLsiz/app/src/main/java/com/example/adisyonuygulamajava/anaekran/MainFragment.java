package com.example.adisyonuygulamajava.anaekran;

import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.adisyonuygulamajava.masadetay.MasaDetayFragment;
import com.example.adisyonuygulamajava.model.Kategori;
import com.example.adisyonuygulamajava.model.Masa;
import com.example.adisyonuygulamajava.viewmodel.MasaDetayViewModel;
import com.example.adisyonuygulamajava.viewmodel.MasalarViewModel;
import com.example.adisyonuygulamajava.viewmodel.UrunViewModel;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainFragment extends Fragment {

    private static final int RESIM_SEC_KODU = 1001;

    private String secilenResimBase64 = null;
    private List<Masa> masaList = new ArrayList<>();
    private LinearLayout containerLayout;
    private MainLayout layout;

    private UrunViewModel urunVM;
    private MasalarViewModel viewModel;

    private final ActivityResultLauncher<String> resimSecLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    secilenResimBase64 = uriToBase64(uri);
                    Toast.makeText(requireContext(), "Resim seçildi", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Serializable args = getArguments() != null ? getArguments().getSerializable("masalar") : null;
        if (args instanceof List) {
            //noinspection unchecked
            masaList = (List<Masa>) args;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        containerLayout = new LinearLayout(requireContext());
        containerLayout.setOrientation(LinearLayout.VERTICAL);

        urunVM = new ViewModelProvider(this).get(UrunViewModel.class);
        viewModel = new ViewModelProvider(this).get(MasalarViewModel.class);

        viewModel.masalariYukle();
        urunVM.kategorileriYukle();

        viewModel.getMasalar().observe(getViewLifecycleOwner(), masaListesi -> {
            containerLayout.removeAllViews();
            layout = new MainLayout(
                    requireContext(),
                    masaListesi,
                    getParentFragmentManager(),
                    new MasaDetayViewModel(0),
                    masa -> getParentFragmentManager()
                            .beginTransaction()
                            .replace((container != null ? container.getId() : View.generateViewId()),
                                    MasaDetayFragment.newInstance(masa.getId()))
                            .addToBackStack(null)
                            .commit()
            );
            containerLayout.addView(layout.linearLayout());
            ekIslemButonlariniEkle(containerLayout);
        });

        return containerLayout;
    }

    public static MainFragment newInstance(List<Masa> masalar) {
        MainFragment fragment = new MainFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("masalar", new ArrayList<>(masalar));
        fragment.setArguments(bundle);
        return fragment;
    }

    private void masaEkleCikarDialog() {
        EditText input = new EditText(requireContext());
        input.setHint("Silinecek masa ID");
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setPadding(50, 40, 50, 20);

        new AlertDialog.Builder(requireContext())
                .setTitle("Masa İşlemleri")
                .setMessage("Yeni masa ekleyebilir veya masa ID girerek masa silebilirsiniz.")
                .setView(input)
                .setPositiveButton("Masa Ekle", (dialog, which) -> {
                    viewModel.masaEkle(() -> {
                        Toast.makeText(requireContext(), "Masa eklendi", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        viewModel.masalariYukle();
                    });
                })
                .setNeutralButton("Masa Sil", (dialog, which) -> {
                    String masaIdText = input.getText().toString();
                    if (!masaIdText.isEmpty()) {
                        try {
                            int masaId = Integer.parseInt(masaIdText);
                            viewModel.masaSil(masaId);
                        } catch (NumberFormatException e) {
                            Toast.makeText(requireContext(), "Geçerli bir ID giriniz", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Masa ID giriniz", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("İptal", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void masaBirlestirDialog() {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        EditText anaIdEt = new EditText(requireContext());
        anaIdEt.setHint("Ana Masa ID");
        anaIdEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        EditText bIdEt = new EditText(requireContext());
        bIdEt.setHint("Birleştirilecek Masa ID");
        bIdEt.setInputType(InputType.TYPE_CLASS_NUMBER);

        layout.addView(anaIdEt);
        layout.addView(bIdEt);

        new AlertDialog.Builder(requireContext())
                .setTitle("Masa Birleştir")
                .setView(layout)
                .setPositiveButton("Birleştir", (dialog, which) -> {
                    try {
                        int anaId = Integer.parseInt(anaIdEt.getText().toString());
                        int bId = Integer.parseInt(bIdEt.getText().toString());
                        viewModel.masaBirlestir(anaId, bId);
                        viewModel.masalariYukle();
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Lütfen geçerli iki ID girin", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("İptal", null)
                .show();
    }

    private void ekIslemButonlariniEkle(LinearLayout container) {
        LinearLayout butonLayout = new LinearLayout(requireContext());
        butonLayout.setOrientation(LinearLayout.HORIZONTAL);
        butonLayout.setGravity(Gravity.CENTER);
        butonLayout.setPadding(20, 20, 20, 20);
        butonLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout.LayoutParams wParams = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        wParams.setMarginStart(10);
        wParams.setMarginEnd(10);

        Button btnMasa = new Button(requireContext());
        btnMasa.setText("Masa İşlemleri");
        btnMasa.setLayoutParams(wParams);
        btnMasa.setOnClickListener(v -> masaEkleCikarDialog());

        Button btnUrun = new Button(requireContext());
        btnUrun.setText("Ürün İşlemleri");
        btnUrun.setLayoutParams(wParams);
        btnUrun.setOnClickListener(v -> {
            List<Kategori> kategoriler = urunVM.getKategoriler().getValue();
            if (kategoriler != null) urunEkleVeSilDialog(kategoriler);
        });

        Button btnBirlestir = new Button(requireContext());
        btnBirlestir.setText("Masa Birleştir");
        btnBirlestir.setLayoutParams(wParams);
        btnBirlestir.setOnClickListener(v -> masaBirlestirDialog());

        Button btnKategori = new Button(requireContext());
        btnKategori.setText("Kategori İşlemleri");
        btnKategori.setLayoutParams(wParams);
        btnKategori.setOnClickListener(v -> {
            List<Kategori> kategoriler = urunVM.getKategoriler().getValue();
            if (kategoriler != null) {
                kategoriEkleVeSilDialog(kategoriler);
                urunVM.kategorileriYukle();
            }
        });

        butonLayout.addView(btnMasa);
        butonLayout.addView(btnUrun);
        butonLayout.addView(btnBirlestir);
        butonLayout.addView(btnKategori);

        container.addView(butonLayout);
    }

    private void kategoriEkleVeSilDialog(List<Kategori> kategoriler) {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 30, 40, 10);

        EditText kategoriAdEt = new EditText(requireContext());
        kategoriAdEt.setHint("Yeni Kategori Adı");
        layout.addView(kategoriAdEt);

        Spinner spinner = new Spinner(requireContext());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                kategoriler.stream().map(Kategori::getKategori_ad).collect(Collectors.toList()));
        spinner.setAdapter(adapter);
        layout.addView(spinner);

        new AlertDialog.Builder(requireContext())
                .setTitle("Kategori İşlemleri")
                .setView(layout)
                .setPositiveButton("Kaydet", (dialog, which) -> {
                    String ad = kategoriAdEt.getText().toString().trim();
                    if (!ad.isEmpty()) {
                        urunVM.kategoriEkle(ad);
                        urunVM.kategorileriYukle();
                    } else {
                        Toast.makeText(requireContext(), "Ad girin!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("Sil", (dialog, which) -> {
                    int pos = spinner.getSelectedItemPosition();
                    int kategoriId = kategoriler.get(pos).getId();
                    urunVM.kategoriSil(kategoriId);
                })
                .setNegativeButton("İptal", null)
                .show();
    }

    private void urunEkleVeSilDialog(List<Kategori> kategoriler) {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 30, 30, 30);

        EditText urunAdEt = new EditText(requireContext());
        urunAdEt.setHint("Ürün Adı");

        EditText fiyatEt = new EditText(requireContext());
        fiyatEt.setHint("Ürün Fiyatı");
        fiyatEt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        Spinner kategoriSpinner = new Spinner(requireContext());
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                kategoriler.stream().map(Kategori::getKategori_ad).collect(Collectors.toList()));
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        kategoriSpinner.setAdapter(spinnerAdapter);

        Button resimButton = new Button(requireContext());
        resimButton.setText("Resim Seç");
        resimButton.setOnClickListener(v -> resimSecLauncher.launch("image/*"));

        EditText silAdEditText = new EditText(requireContext());
        silAdEditText.setHint("Silinecek ürünün adı");

        layout.addView(urunAdEt);
        layout.addView(fiyatEt);
        layout.addView(kategoriSpinner);
        layout.addView(resimButton);
        layout.addView(new View(requireContext())); // Ayırıcı çizgi yerine
        layout.addView(silAdEditText);

        new AlertDialog.Builder(requireContext())
                .setView(layout)
                .setPositiveButton("Ekle", (dialog, which) -> {
                    String ad = urunAdEt.getText().toString();
                    float fiyat = Float.parseFloat(fiyatEt.getText().toString());
                    int kategoriId = kategoriler.get(kategoriSpinner.getSelectedItemPosition()).getId();
                    if (secilenResimBase64 != null) {
                        urunVM.urunEkle(ad, fiyat, secilenResimBase64, kategoriId);
                        urunVM.urunleriYukle();
                    }
                })
                .setNeutralButton("Sil", (dialog, which) -> {
                    String ad = silAdEditText.getText().toString();
                    if (!ad.isEmpty()) {
                        urunVM.urunSil(ad);
                        urunVM.urunleriYukle();
                    } else {
                        Toast.makeText(requireContext(), "Lütfen silinecek ürün adını girin!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("İptal", null)
                .show();
    }

    private String uriToBase64(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            byte[] bytes = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bytes = inputStream != null ? inputStream.readAllBytes() : null;
            }
            if (inputStream != null) inputStream.close();
            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
