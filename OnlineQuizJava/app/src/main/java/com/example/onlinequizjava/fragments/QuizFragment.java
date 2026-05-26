package com.example.onlinequizjava.fragments;


import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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

public class QuizFragment extends Fragment implements SocketEventListener {

    /*
        Quiz ekranı.

        Bu ekran:
        - new_question mesajını alır
        - Soruyu ve şıkları gösterir
        - Cevap gönderir
        - Cevabı renklendirir
        - Süre bitince doğru cevabı gösterir
        - Quiz bitince WinnerFragment'a geçer
    */

    private TextView txtQuestionCounter;
    private TextView txtTimer;
    private TextView txtQuestionText;
    private TextView txtAnswerResult;
    private TextView txtScoreboard;
    private LinearLayout optionsContainer;

    private String roomCode = "";
    private String username = "";
    private int questionTime = 20;
    private boolean isOwner = false;

    private boolean answeredCurrentQuestion = false;
    private int selectedAnswerIndex = -1;
    private int currentCorrectIndex = -1;

    private final List<Button> optionButtons = new ArrayList<>();

    private CountDownTimer countDownTimer;

    public QuizFragment() {
        super(R.layout.fragment_quiz);
    }

    public static QuizFragment newInstance(
            String roomCode,
            String username,
            int questionTime,
            boolean isOwner
    ) {
        QuizFragment fragment = new QuizFragment();

        Bundle bundle = new Bundle();
        bundle.putString("roomCode", roomCode);
        bundle.putString("username", username);
        bundle.putInt("questionTime", questionTime);
        bundle.putBoolean("isOwner", isOwner);

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = requireArguments();

        roomCode = args.getString("roomCode", "");
        username = args.getString("username", "");
        questionTime = args.getInt("questionTime", 20);
        isOwner = args.getBoolean("isOwner", false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        txtQuestionCounter = view.findViewById(R.id.txtQuestionCounter);
        txtTimer = view.findViewById(R.id.txtTimer);
        txtQuestionText = view.findViewById(R.id.txtQuestionText);
        txtAnswerResult = view.findViewById(R.id.txtAnswerResult);
        txtScoreboard = view.findViewById(R.id.txtScoreboard);
        optionsContainer = view.findViewById(R.id.optionsContainer);

        WebSocketManager.getInstance().setListener(this);

        txtQuestionCounter.setText("Soru bekleniyor...");
        txtTimer.setText("Süre: " + questionTime);
        txtQuestionText.setText("Quiz başladı. İlk soru bekleniyor.");
        txtAnswerResult.setText("");
    }

    @Override
    public void onSocketConnected() {

    }

    @Override
    public void onSocketMessage(String message) {
        try {
            JSONObject json = new JSONObject(message);
            String type = json.optString("type");

            if ("new_question".equals(type)) {
                handleNewQuestion(json);

            } else if ("answer_result".equals(type)) {
                handleAnswerResult(json);

            } else if ("scoreboard_updated".equals(type)) {
                JSONArray scoreboard = json.optJSONArray("scoreboard");
                txtScoreboard.setText(buildScoreboardText(scoreboard));

            } else if ("time_up".equals(type)) {
                handleTimeUp(json);

            } else if ("quiz_finished".equals(type)) {
                handleQuizFinished(json);

            } else if ("error".equals(type)) {
                txtAnswerResult.setText(json.optString("message", "Bilinmeyen hata oluştu."));

            } else if ("answer_rejected".equals(type)) {
                txtAnswerResult.setText(json.optString("message", "Cevap reddedildi."));
            }

        } catch (Exception e) {
            txtAnswerResult.setText("JSON okuma hatası: " + e.getMessage());
        }
    }

    private void handleNewQuestion(JSONObject json) {
        answeredCurrentQuestion = false;
        selectedAnswerIndex = -1;
        currentCorrectIndex = -1;

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        txtAnswerResult.setText("");

        int questionNumber = json.optInt("question_number");
        int totalQuestions = json.optInt("total_questions");
        String questionText = json.optString("question_text");
        JSONArray options = json.optJSONArray("options");
        int serverQuestionTime = json.optInt("question_time", questionTime);
        JSONArray scoreboard = json.optJSONArray("scoreboard");

        questionTime = serverQuestionTime;

        txtQuestionCounter.setText("Soru " + questionNumber + " / " + totalQuestions);
        txtQuestionText.setText(questionText);
        txtScoreboard.setText(buildScoreboardText(scoreboard));

        if (options == null) {
            options = new JSONArray();
        }

        renderOptions(options);

        startLocalTimer(questionTime);
    }

    private void renderOptions(JSONArray options) {
        optionsContainer.removeAllViews();
        optionButtons.clear();

        for (int i = 0; i < options.length(); i++) {
            String optionText = options.optString(i);

            Button button = new Button(requireContext());

            button.setText(indexToLetter(i) + ") " + optionText);
            button.setTextSize(16f);
            button.setAllCaps(false);
            button.setTextColor(Color.parseColor("#111827"));
            button.setTypeface(Typeface.DEFAULT_BOLD);
            button.setBackground(createOptionBackground(
                    Color.WHITE,
                    Color.parseColor("#D1D5DB")
            ));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(58)
            );

            params.setMargins(0, 0, 0, dpToPx(14));

            button.setLayoutParams(params);

            final int answerIndex = i;

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    submitAnswer(answerIndex);
                }
            });

            optionButtons.add(button);
            optionsContainer.addView(button);
        }
    }

    private void submitAnswer(int answerIndex) {
        if (answeredCurrentQuestion) {
            txtAnswerResult.setText("Bu soruya zaten cevap verdin.");
            return;
        }

        answeredCurrentQuestion = true;
        selectedAnswerIndex = answerIndex;

        markOptionAsSelectedWaiting(answerIndex);
        setOptionButtonsEnabled(false);

        String message = SocketMessageFactory.submitAnswer(
                roomCode,
                username,
                answerIndex
        );

        WebSocketManager.getInstance().sendMessage(message);

        txtAnswerResult.setText("Cevabın gönderildi...");
    }

    private void handleAnswerResult(JSONObject json) {
        boolean isCorrect = json.optBoolean("is_correct");
        int earnedScore = json.optInt("earned_score");
        int totalScore = json.optInt("total_score");

        if (selectedAnswerIndex >= 0) {
            if (isCorrect) {
                markOptionAsCorrect(selectedAnswerIndex);
            } else {
                markOptionAsWrong(selectedAnswerIndex);
            }
        }

        if (isCorrect) {
            txtAnswerResult.setText(
                    "Doğru cevap! +" + earnedScore + " puan | Toplam: " + totalScore
            );
        } else {
            txtAnswerResult.setText("Yanlış cevap. Puan kazanamadın.");
        }
    }

    private void handleTimeUp(JSONObject json) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        setOptionButtonsEnabled(false);

        currentCorrectIndex = json.optInt("correct_index", -1);

        JSONArray scoreboard = json.optJSONArray("scoreboard");

        txtTimer.setText("Süre bitti");

        if (currentCorrectIndex >= 0) {
            markOptionAsCorrect(currentCorrectIndex);
        }

        if (
                selectedAnswerIndex >= 0 &&
                        currentCorrectIndex >= 0 &&
                        selectedAnswerIndex != currentCorrectIndex
        ) {
            markOptionAsWrong(selectedAnswerIndex);
        }

        if (currentCorrectIndex >= 0) {
            txtAnswerResult.setText(
                    "Süre bitti. Doğru cevap: " + indexToLetter(currentCorrectIndex)
            );
        } else {
            txtAnswerResult.setText("Süre bitti.");
        }

        txtScoreboard.setText(buildScoreboardText(scoreboard));
    }

    private void handleQuizFinished(JSONObject json) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        JSONArray winners = json.optJSONArray("winners");
        JSONArray scoreboard = json.optJSONArray("scoreboard");

        if (winners == null) {
            winners = new JSONArray();
        }

        if (scoreboard == null) {
            scoreboard = new JSONArray();
        }

        ((MainActivity) requireActivity()).openWinnerFragment(
                winners.toString(),
                scoreboard.toString()
        );
    }

    private void startLocalTimer(int seconds) {
        txtTimer.setText("Süre: " + seconds);

        countDownTimer = new CountDownTimer(seconds * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                int remaining = (int) (millisUntilFinished / 1000L);
                txtTimer.setText("Süre: " + remaining);
            }

            @Override
            public void onFinish() {
                txtTimer.setText("Süre bitti");
                setOptionButtonsEnabled(false);
            }
        };

        countDownTimer.start();
    }

    private void setOptionButtonsEnabled(boolean enabled) {
        for (Button button : optionButtons) {
            button.setEnabled(enabled);
        }
    }

    private void markOptionAsSelectedWaiting(int index) {
        Button button = getOptionButton(index);

        if (button == null) {
            return;
        }

        button.setBackground(createOptionBackground(
                Color.parseColor("#FEF3C7"),
                Color.parseColor("#F59E0B")
        ));

        button.setTextColor(Color.parseColor("#92400E"));
    }

    private void markOptionAsCorrect(int index) {
        Button button = getOptionButton(index);

        if (button == null) {
            return;
        }

        button.setBackground(createOptionBackground(
                Color.parseColor("#DCFCE7"),
                Color.parseColor("#16A34A")
        ));

        button.setTextColor(Color.parseColor("#166534"));
    }

    private void markOptionAsWrong(int index) {
        Button button = getOptionButton(index);

        if (button == null) {
            return;
        }

        button.setBackground(createOptionBackground(
                Color.parseColor("#FEE2E2"),
                Color.parseColor("#DC2626")
        ));

        button.setTextColor(Color.parseColor("#991B1B"));
    }

    private Button getOptionButton(int index) {
        if (index < 0 || index >= optionButtons.size()) {
            return null;
        }

        return optionButtons.get(index);
    }

    private GradientDrawable createOptionBackground(
            int backgroundColor,
            int strokeColor
    ) {
        GradientDrawable drawable = new GradientDrawable();

        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(dpToPx(14));
        drawable.setColor(backgroundColor);
        drawable.setStroke(dpToPx(1), strokeColor);

        return drawable;
    }

    private String buildScoreboardText(JSONArray scoreboard) {
        if (scoreboard == null || scoreboard.length() == 0) {
            return "Puan tablosu bekleniyor...";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Puan Tablosu:\n\n");

        for (int i = 0; i < scoreboard.length(); i++) {
            JSONObject item = scoreboard.optJSONObject(i);

            if (item == null) {
                continue;
            }

            String name = item.optString("username", "-");
            int score = item.optInt("score", 0);

            builder.append(i + 1)
                    .append(". ")
                    .append(name)
                    .append(" - ")
                    .append(score)
                    .append(" puan\n");
        }

        return builder.toString();
    }

    private String indexToLetter(int index) {
        if (index >= 0 && index < 26) {
            return String.valueOf((char) ('A' + index));
        }

        return String.valueOf(index + 1);
    }

    @Override
    public void onSocketDisconnected() {
        txtAnswerResult.setText("Sunucu bağlantısı kapandı.");
    }

    @Override
    public void onSocketError(String error) {
        txtAnswerResult.setText("Bağlantı hatası: " + error);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        WebSocketManager.getInstance().removeListener(this);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
