package com.example.onlinequizjava.fragments;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.example.onlinequizjava.MainActivity;
import com.example.onlinequizjava.R;
import com.example.onlinequizjava.network.WebSocketManager;

import org.json.JSONArray;
import org.json.JSONObject;

public class WinnerFragment extends Fragment {

    private TextView txtWinners;
    private TextView txtFinalScoreboard;

    private String winnersJson = "[]";
    private String scoreboardJson = "[]";

    public WinnerFragment() {
        super(R.layout.fragment_winner);
    }

    public static WinnerFragment newInstance(
            String winnersJson,
            String scoreboardJson
    ) {
        WinnerFragment fragment = new WinnerFragment();

        Bundle bundle = new Bundle();
        bundle.putString("winnersJson", winnersJson);
        bundle.putString("scoreboardJson", scoreboardJson);

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = requireArguments();

        winnersJson = args.getString("winnersJson", "[]");
        scoreboardJson = args.getString("scoreboardJson", "[]");
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        txtWinners = view.findViewById(R.id.txtWinners);
        txtFinalScoreboard = view.findViewById(R.id.txtFinalScoreboard);

        Button btnBackHome = view.findViewById(R.id.btnBackHome);

        try {
            txtWinners.setText(buildWinnersText(new JSONArray(winnersJson)));
            txtFinalScoreboard.setText(buildFinalScoreboardText(new JSONArray(scoreboardJson)));
        } catch (Exception e) {
            txtWinners.setText("Kazanan bilgisi okunamadı.");
            txtFinalScoreboard.setText(e.getMessage());
        }

        btnBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WebSocketManager.getInstance().disconnect();
                ((MainActivity) requireActivity()).openHomeFragment();
            }
        });
    }

    private String buildWinnersText(JSONArray winners) {
        if (winners.length() == 0) {
            return "Kazanan bulunamadı.";
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < winners.length(); i++) {
            JSONObject item = winners.optJSONObject(i);

            if (item == null) {
                continue;
            }

            String username = item.optString("username", "-");
            int score = item.optInt("score", 0);

            String medal;

            if (i == 0) {
                medal = "🥇";
            } else if (i == 1) {
                medal = "🥈";
            } else if (i == 2) {
                medal = "🥉";
            } else {
                medal = "";
            }

            builder.append(medal)
                    .append(" ")
                    .append(username)
                    .append("\n")
                    .append(score)
                    .append(" puan\n\n");
        }

        return builder.toString().trim();
    }

    private String buildFinalScoreboardText(JSONArray scoreboard) {
        if (scoreboard.length() == 0) {
            return "Puan tablosu yok.";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Genel Sıralama:\n\n");

        for (int i = 0; i < scoreboard.length(); i++) {
            JSONObject item = scoreboard.optJSONObject(i);

            if (item == null) {
                continue;
            }

            String username = item.optString("username", "-");
            int score = item.optInt("score", 0);

            builder.append(i + 1)
                    .append(". ")
                    .append(username)
                    .append(" - ")
                    .append(score)
                    .append(" puan\n");
        }

        return builder.toString();
    }
}
