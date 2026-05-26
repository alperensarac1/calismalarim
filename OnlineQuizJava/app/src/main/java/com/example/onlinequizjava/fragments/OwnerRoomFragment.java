package com.example.onlinequizjava.fragments;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.onlinequizjava.MainActivity;
import com.example.onlinequizjava.R;
import com.example.onlinequizjava.network.SocketEventListener;
import com.example.onlinequizjava.network.SocketMessageFactory;
import com.example.onlinequizjava.network.WebSocketManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OwnerRoomFragment extends Fragment implements SocketEventListener {

    /*
        Oda sahibi ekranı.

        Bu ekranda:
        - Oda kodu gösterilir
        - Oyuncu listesi gösterilir
        - Dinamik şık eklenebilir
        - Soru server'a gönderilir
        - Quiz başlatılır
    */

    private TextView txtRoomCode;
    private TextView txtInfo;
    private TextView txtPlayers;
    private TextView txtQuestionCount;
    private TextView txtStatus;

    private EditText edtQuestionText;
    private LinearLayout optionsContainer;

    private String roomCode = "";
    private String username = "";
    private int questionTime = 20;

    private int questionCount = 0;

    private final List<OptionRow> optionRows = new ArrayList<>();

    private int selectedOptionIndex = -1;

    public OwnerRoomFragment() {
        super(R.layout.fragment_owner_room);
    }

    public static OwnerRoomFragment newInstance(
            String roomCode,
            String username,
            int questionTime
    ) {
        OwnerRoomFragment fragment = new OwnerRoomFragment();

        Bundle bundle = new Bundle();
        bundle.putString("roomCode", roomCode);
        bundle.putString("username", username);
        bundle.putInt("questionTime", questionTime);

        fragment.setArguments(bundle);

        return fragment;
    }

    private static class OptionRow {
        LinearLayout rowLayout;
        RadioButton radioButton;
        EditText editText;
        Button removeButton;

        OptionRow(
                LinearLayout rowLayout,
                RadioButton radioButton,
                EditText editText,
                Button removeButton
        ) {
            this.rowLayout = rowLayout;
            this.radioButton = radioButton;
            this.editText = editText;
            this.removeButton = removeButton;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = requireArguments();

        roomCode = args.getString("roomCode", "");
        username = args.getString("username", "");
        questionTime = args.getInt("questionTime", 20);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        txtRoomCode = view.findViewById(R.id.txtRoomCode);
        txtInfo = view.findViewById(R.id.txtInfo);
        txtPlayers = view.findViewById(R.id.txtPlayers);
        txtQuestionCount = view.findViewById(R.id.txtQuestionCount);
        txtStatus = view.findViewById(R.id.txtStatus);

        edtQuestionText = view.findViewById(R.id.edtQuestionText);
        optionsContainer = view.findViewById(R.id.optionsContainer);

        Button btnAddOption = view.findViewById(R.id.btnAddOption);
        Button btnAddQuestion = view.findViewById(R.id.btnAddQuestion);
        Button btnStartQuiz = view.findViewById(R.id.btnStartQuiz);

        WebSocketManager.getInstance().setListener(this);

        txtRoomCode.setText("Oda Kodu: " + roomCode);

        txtInfo.setText(
                "Kullanıcı: " + username + "\n" +
                        "Soru Süresi: " + questionTime + " saniye\n\n" +
                        "Bu kodu diğer kullanıcılara ver.\n" +
                        "Onlar bu kod ile odaya katılacak."
        );

        /*
            Başlangıçta minimum 2 şık açıyoruz.
        */
        addOptionRow("");
        addOptionRow("");

        btnAddOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addOptionRow("");
            }
        });

        btnAddQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addQuestion();
            }
        });

        btnStartQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startQuiz();
            }
        });
    }

    private void addOptionRow(String defaultText) {
        LinearLayout rowLayout = new LinearLayout(requireContext());
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setGravity(Gravity.CENTER_VERTICAL);
        rowLayout.setPadding(0, dpToPx(8), 0, dpToPx(8));

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        rowLayout.setLayoutParams(rowParams);

        RadioButton radioButton = new RadioButton(requireContext());

        LinearLayout.LayoutParams radioParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        radioButton.setLayoutParams(radioParams);

        EditText editText = new EditText(requireContext());
        editText.setHint("Şık " + (optionRows.size() + 1));
        editText.setText(defaultText);
        editText.setSingleLine(true);
        editText.setPadding(dpToPx(14), 0, dpToPx(14), 0);
        editText.setBackgroundColor(0xFFFFFFFF);

        LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(
                0,
                dpToPx(54),
                1f
        );

        editParams.setMargins(dpToPx(8), 0, dpToPx(8), 0);
        editText.setLayoutParams(editParams);

        Button btnRemove = new Button(requireContext());
        btnRemove.setText("Sil");
        btnRemove.setAllCaps(false);
        btnRemove.setTextSize(13f);

        LinearLayout.LayoutParams removeParams = new LinearLayout.LayoutParams(
                dpToPx(72),
                dpToPx(54)
        );

        btnRemove.setLayoutParams(removeParams);

        rowLayout.addView(radioButton);
        rowLayout.addView(editText);
        rowLayout.addView(btnRemove);

        optionsContainer.addView(rowLayout);

        OptionRow optionRow = new OptionRow(
                rowLayout,
                radioButton,
                editText,
                btnRemove
        );

        optionRows.add(optionRow);

        radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectOption(optionRow);
            }
        });

        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeOptionRow(optionRow);
            }
        });

        updateOptionHints();
    }

    private void selectOption(OptionRow selectedRow) {
        for (int i = 0; i < optionRows.size(); i++) {
            OptionRow row = optionRows.get(i);

            boolean selected = row == selectedRow;
            row.radioButton.setChecked(selected);

            if (selected) {
                selectedOptionIndex = i;
            }
        }
    }

    private void removeOptionRow(OptionRow optionRow) {
        if (optionRows.size() <= 2) {
            txtStatus.setText("En az 2 şık kalmalı.");
            return;
        }

        int removedIndex = optionRows.indexOf(optionRow);

        optionsContainer.removeView(optionRow.rowLayout);
        optionRows.remove(optionRow);

        if (selectedOptionIndex == removedIndex) {
            selectedOptionIndex = -1;
            clearRadioSelection();
        } else if (selectedOptionIndex > removedIndex) {
            selectedOptionIndex--;
        }

        updateOptionHints();
    }

    private void clearRadioSelection() {
        for (OptionRow row : optionRows) {
            row.radioButton.setChecked(false);
        }
    }

    private void updateOptionHints() {
        for (int i = 0; i < optionRows.size(); i++) {
            optionRows.get(i).editText.setHint("Şık " + (i + 1));
        }
    }

    private void addQuestion() {
        String questionText = edtQuestionText.getText().toString().trim();

        if (questionText.isEmpty()) {
            txtStatus.setText("Soru metni boş olamaz.");
            return;
        }

        if (selectedOptionIndex == -1) {
            txtStatus.setText("Doğru cevabı seçmelisin.");
            return;
        }

        List<String> filledOptions = new ArrayList<>();
        int correctIndexInFilledOptions = -1;

        for (int i = 0; i < optionRows.size(); i++) {
            String optionText = optionRows.get(i).editText.getText().toString().trim();

            if (!optionText.isEmpty()) {
                if (i == selectedOptionIndex) {
                    correctIndexInFilledOptions = filledOptions.size();
                }

                filledOptions.add(optionText);
            }
        }

        if (filledOptions.size() < 2) {
            txtStatus.setText("En az 2 dolu şık girmelisin.");
            return;
        }

        if (correctIndexInFilledOptions == -1) {
            txtStatus.setText("Doğru cevap olarak seçtiğin şık boş olamaz.");
            return;
        }

        String message = SocketMessageFactory.addQuestion(
                roomCode,
                questionText,
                filledOptions,
                correctIndexInFilledOptions
        );

        WebSocketManager.getInstance().sendMessage(message);

        txtStatus.setText("Soru gönderildi...");
    }

    private void clearQuestionForm() {
        edtQuestionText.setText("");

        optionsContainer.removeAllViews();
        optionRows.clear();
        selectedOptionIndex = -1;

        addOptionRow("");
        addOptionRow("");
    }

    private void startQuiz() {
        if (questionCount <= 0) {
            txtStatus.setText("Quiz başlatmak için en az 1 soru eklemelisin.");
            return;
        }

        String message = SocketMessageFactory.startQuiz(roomCode);

        WebSocketManager.getInstance().sendMessage(message);

        txtStatus.setText("Quiz başlatma isteği gönderildi...");
    }

    @Override
    public void onSocketConnected() {
        /*
            Bu ekrana gelindiğinde bağlantı genelde zaten açıktır.
        */
    }

    @Override
    public void onSocketMessage(String message) {
        try {
            JSONObject json = new JSONObject(message);
            String type = json.optString("type");

            if ("player_list_updated".equals(type)) {
                JSONArray players = json.optJSONArray("players");
                txtPlayers.setText(buildPlayerText(players));

            } else if ("question_added".equals(type)) {
                questionCount = json.optInt("question_count", questionCount + 1);

                txtQuestionCount.setText("Eklenen soru: " + questionCount);
                txtStatus.setText(json.optString("message", "Soru eklendi."));

                clearQuestionForm();

            } else if ("room_question_count_updated".equals(type)) {
                questionCount = json.optInt("question_count", questionCount);
                txtQuestionCount.setText("Eklenen soru: " + questionCount);

            } else if ("quiz_started".equals(type)) {
                txtStatus.setText("Quiz başladı.");

                ((MainActivity) requireActivity()).openQuizFragment(
                        roomCode,
                        username,
                        questionTime,
                        true
                );

            } else if ("error".equals(type)) {
                txtStatus.setText(json.optString("message", "Bilinmeyen hata oluştu."));
            }

        } catch (Exception e) {
            txtStatus.setText("JSON okuma hatası: " + e.getMessage());
        }
    }

    private String buildPlayerText(JSONArray players) {
        if (players == null || players.length() == 0) {
            return "Oyuncular bekleniyor...";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Oyuncular:\n\n");

        for (int i = 0; i < players.length(); i++) {
            builder.append(i + 1)
                    .append(". ")
                    .append(players.optString(i))
                    .append("\n");
        }

        return builder.toString();
    }

    @Override
    public void onSocketDisconnected() {
        txtStatus.setText("Sunucu bağlantısı kapandı.");
    }

    @Override
    public void onSocketError(String error) {
        txtStatus.setText("Bağlantı hatası: " + error);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        WebSocketManager.getInstance().removeListener(this);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
