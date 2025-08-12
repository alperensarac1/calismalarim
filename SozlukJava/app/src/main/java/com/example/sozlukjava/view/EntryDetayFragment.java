package com.example.sozlukjava.view;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sozlukjava.R;
import com.example.sozlukjava.databinding.FragmentEntryDetayBinding;
import com.example.sozlukjava.model.Comment;
import com.example.sozlukjava.util.SessionManager;
import com.example.sozlukjava.viewmodel.EntryDetayViewModel;

// EntryDetayFragment.java
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

public class EntryDetayFragment extends Fragment {
    private FragmentEntryDetayBinding binding;

    private EntryDetayViewModel viewModel;
    private SessionManager session;
    private ArrayAdapter<String> adapter;

    private int entryId = -1;
    private List<Comment> commentList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEntryDetayBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = new SessionManager(requireContext());
        viewModel = new ViewModelProvider(this).get(EntryDetayViewModel.class);

        entryId = getArguments() != null ? getArguments().getInt("entryId", -1) : -1;
        if (entryId == -1) return;

        viewModel.loadEntry(entryId);

        viewModel.getEntry().observe(getViewLifecycleOwner(), e -> {
            if (e == null) return;
            binding.tvEntryTitle.setText(e.getTitle());
            binding.tvEntryContent.setText(e.getContent());
            binding.tvEntryAuthor.setText(e.getUsername());
            String created = e.getCreated_at();
            binding.tvEntryDate.setText(created != null && created.length() >= 10 ? created.substring(0, 10) : "");
        });

        // Yorumları yükle
        viewModel.loadComments(entryId);

        viewModel.getComments().observe(getViewLifecycleOwner(), list -> {
            commentList = list != null ? list : new ArrayList<>();
            ArrayList<String> strings = new ArrayList<>();
            for (Comment c : commentList) {
                strings.add(c.getUsername() + ":\n" + c.getComment_text() +
                        "\n👍" + c.getLikes() + " 👎" + c.getDislikes());
            }
            adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_list_item_1, strings);
            binding.commentListView.setAdapter(adapter);
        });

        binding.btnYorumGonder.setOnClickListener(v -> {
            String commentText = binding.etComment.getText().toString();
            int userId = session.getUserId();
            if (commentText != null && !commentText.trim().isEmpty()) {
                viewModel.addComment(entryId, userId, commentText);
                binding.etComment.getText().clear();
            }
        });

        binding.commentListView.setOnItemClickListener((parent, v1, position, id) -> {
            Comment comment = commentList.get(position);
            showLikeDislikeDialog(comment);
        });
    }

    private void showLikeDislikeDialog(Comment comment) {
        int userId = session.getUserId();
        String[] options = new String[]{"👍 Beğen", "👎 Beğenme"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Yorumu Oyla")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        viewModel.voteComment(entryId, comment.getId(), userId, true);
                    } else if (which == 1) {
                        viewModel.voteComment(entryId, comment.getId(), userId, false);
                    }
                })
                .setNegativeButton("İptal", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
