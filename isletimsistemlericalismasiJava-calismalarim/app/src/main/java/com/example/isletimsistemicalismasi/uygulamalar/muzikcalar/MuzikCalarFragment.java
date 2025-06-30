package com.example.isletimsistemicalismasi.uygulamalar.muzikcalar;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.isletimsistemicalismasi.R;
import com.example.isletimsistemicalismasi.databinding.FragmentMuzikCalarBinding;

import java.util.ArrayList;


public class MuzikCalarFragment extends Fragment {

    FragmentMuzikCalarBinding binding;
    private MediaPlayer mediaPlayer;
    private ArrayList<Integer> songList;
    private ArrayList<String> songNames;
    private int currentSongIndex = 0;
    ArrayAdapter<String> adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMuzikCalarBinding.inflate(inflater,container,false);




        songList = new ArrayList<>();
        songList.add(R.raw.vidya);
        songList.add(R.raw.shine);
        songList.add(R.raw.disfigure);


        songNames = new ArrayList<>();
        songNames.add("Vidya");
        songNames.add("Shine");
        songNames.add("Disfigure");


        mediaPlayer = MediaPlayer.create(requireActivity(), songList.get(currentSongIndex));
       binding.musicProgressBar.setMax(mediaPlayer.getDuration());
        binding.songTitle.setText(songNames.get(currentSongIndex));
        binding.buttonPreview.setOnClickListener(v -> playPreviousSong());

        adapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1,android.R.id.text1,songNames);
        binding.lvMuzikCalar.setAdapter(adapter);
        binding.lvMuzikCalar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mediaPlayer.stop();
                currentSongIndex = position;
                mediaPlayer = MediaPlayer.create(requireActivity(), songList.get(currentSongIndex));
                mediaPlayer.start();
                binding.songTitle.setText(songNames.get(currentSongIndex));
                binding.musicProgressBar.setMax(mediaPlayer.getDuration());
                updateSeekBar();
            }
        });

        binding.buttonPlay.setOnClickListener(v -> {
            mediaPlayer.start();
            updateSeekBar();
        });


        binding.buttonPause.setOnClickListener(v -> mediaPlayer.pause());


        binding.buttonStop.setOnClickListener(v -> {
            mediaPlayer.stop();
            mediaPlayer.prepareAsync();
            binding.musicProgressBar.setProgress(0);
        });


        binding.buttonNext.setOnClickListener(v -> playNextSong());


        binding.musicProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        return binding.getRoot();
    }

    private void playNextSong() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        currentSongIndex = (currentSongIndex + 1) % songList.size();
        mediaPlayer = MediaPlayer.create(requireActivity(), songList.get(currentSongIndex));
        mediaPlayer.start();
        binding.songTitle.setText(songNames.get(currentSongIndex));
        binding.musicProgressBar.setMax(mediaPlayer.getDuration());
        updateSeekBar();
    }


    private void playPreviousSong() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        currentSongIndex = (currentSongIndex - 1 + songList.size()) % songList.size();
        mediaPlayer = MediaPlayer.create(requireActivity(), songList.get(currentSongIndex));
        mediaPlayer.start();
        binding.songTitle.setText(songNames.get(currentSongIndex));
        binding.musicProgressBar.setMax(mediaPlayer.getDuration());
        updateSeekBar();
    }


    private void updateSeekBar() {
        new Thread(() -> {
            while (mediaPlayer != null && mediaPlayer.isPlaying()) {
                try {
                    Thread.sleep(1000);
                    requireActivity().runOnUiThread(() -> binding.musicProgressBar.setProgress(mediaPlayer.getCurrentPosition()));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
