package com.example.yardimuygulamajava;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import android.os.Bundle;

import com.example.yardimuygulamajava.entity.Session;
import com.example.yardimuygulamajava.view.HelperOpenListFragment;
import com.example.yardimuygulamajava.view.LoginFragment;
import com.example.yardimuygulamajava.view.PatientHelpFragment;

public class MainActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            Fragment start;
            if (Session.isLoggedIn(this)) {
                long id = Session.userId(this);
                String role = Session.role(this);
                if ("YARDIMCI".equals(role)) start = HelperOpenListFragment.newInstance(id);
                else start = PatientHelpFragment.newInstance(id);
            } else {
                start = new LoginFragment();
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, start)
                    .commit();
        }
    }
}