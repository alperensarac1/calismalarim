package com.example.yardimuygulamajava.view;


import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


import com.example.yardimuygulamajava.R;
import com.example.yardimuygulamajava.entity.Session;
import com.example.yardimuygulamajava.repo.AuthRepo;
import com.example.yardimuygulamajava.service.ApiOk;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private final AuthRepo repo = new AuthRepo();

    public LoginFragment() { super(R.layout.fragment_login); }

    @Override public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        EditText etPhone = view.findViewById(R.id.etPhone);
        EditText etPass = view.findViewById(R.id.etPass);
        Button btnLogin = view.findViewById(R.id.btnLogin);
        Button btnGoRegister = view.findViewById(R.id.btnGoRegister);
        ProgressBar progress = view.findViewById(R.id.progress);
        TextView tvInfo = view.findViewById(R.id.tvInfo);

        btnGoRegister.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new RegisterFragment())
                        .addToBackStack(null)
                        .commit()
        );

        btnLogin.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            String pass = etPass.getText().toString().trim();
            if (phone.isEmpty() || pass.isEmpty()) {
                tvInfo.setText("Telefon ve şifre zorunlu");
                return;
            }

            progress.setVisibility(View.VISIBLE);
            tvInfo.setText("");

            repo.login(phone, pass).enqueue(new Callback<ApiOk<Object>>() {
                @Override public void onResponse(Call<ApiOk<Object>> call, Response<ApiOk<Object>> resp) {
                    progress.setVisibility(View.GONE);
                    ApiOk<Object> res = resp.body();
                    if (resp.isSuccessful() && res != null && res.getOk() && res.user != null) {
                        Session.save(requireContext(), res.user.id, res.user.role);
                        if ("YARDIMCI".equals(res.user.role)) {
                            go(HelperOpenListFragment.newInstance(res.user.id));
                        } else {
                            go(PatientHelpFragment.newInstance(res.user.id));
                        }
                    } else {
                        tvInfo.setText(res != null ? (res.error != null ? res.error : "Giriş başarısız") : "Giriş başarısız");
                    }
                }

                @Override public void onFailure(Call<ApiOk<Object>> call, Throwable t) {
                    progress.setVisibility(View.GONE);
                    tvInfo.setText("Bağlantı hatası: " + t.getMessage());
                }
            });
        });
    }

    private void go(Fragment f) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, f)
                .commit();
    }
}
