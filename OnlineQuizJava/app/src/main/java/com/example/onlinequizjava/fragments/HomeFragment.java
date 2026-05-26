package com.example.onlinequizjava.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.onlinequizjava.MainActivity;
import com.example.onlinequizjava.R;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    /*
        Ana ekran.

        Kullanıcı:
        - Oda oluşturabilir
        - Var olan odaya katılabilir
    */

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        Button btnCreateRoom = view.findViewById(R.id.btnCreateRoom);
        Button btnJoinRoom = view.findViewById(R.id.btnJoinRoom);

        btnCreateRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) requireActivity()).openCreateRoomFragment();
            }
        });

        btnJoinRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) requireActivity()).openJoinRoomFragment();
            }
        });
    }
}
