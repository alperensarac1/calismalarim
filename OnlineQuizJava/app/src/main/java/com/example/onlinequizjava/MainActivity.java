package com.example.onlinequizjava;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.onlinequizjava.fragments.CreateRoomFragment;
import com.example.onlinequizjava.fragments.HomeFragment;
import com.example.onlinequizjava.fragments.JoinRoomFragment;
import com.example.onlinequizjava.fragments.OwnerRoomFragment;
import com.example.onlinequizjava.fragments.QuizFragment;
import com.example.onlinequizjava.fragments.WaitingRoomFragment;
import com.example.onlinequizjava.fragments.WinnerFragment;


public class MainActivity extends AppCompatActivity {

    /*
        Bu projede tek Activity kullanıyoruz.

        Ekranların tamamı Fragment olarak değiştiriliyor.

        Avantaj:
        - WebSocket bağlantısı uygulama içinde daha kolay yönetilir.
        - Ekran geçişleri sadeleşir.
        - Quiz akışında Fragment değiştirmek yeterli olur.
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            openHomeFragment();
        }
    }

    private void replaceFragment(Fragment fragment, boolean addToBackStack) {
        androidx.fragment.app.FragmentTransaction transaction =
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }

    public void openHomeFragment() {
        replaceFragment(new HomeFragment(), false);
    }

    public void openCreateRoomFragment() {
        replaceFragment(new CreateRoomFragment(), true);
    }

    public void openJoinRoomFragment() {
        replaceFragment(new JoinRoomFragment(), true);
    }

    public void openOwnerRoomFragment(String roomCode, String username, int questionTime) {
        replaceFragment(
                OwnerRoomFragment.newInstance(roomCode, username, questionTime),
                true
        );
    }

    public void openWaitingRoomFragment(String roomCode, String username, int questionTime) {
        replaceFragment(
                WaitingRoomFragment.newInstance(roomCode, username, questionTime),
                true
        );
    }

    public void openQuizFragment(
            String roomCode,
            String username,
            int questionTime,
            boolean isOwner
    ) {
        replaceFragment(
                QuizFragment.newInstance(roomCode, username, questionTime, isOwner),
                true
        );
    }

    public void openWinnerFragment(String winnersJson, String scoreboardJson) {
        replaceFragment(
                WinnerFragment.newInstance(winnersJson, scoreboardJson),
                false
        );
    }
}