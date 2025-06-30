package com.example.isletimsistemlericalismasikotlin.uygulamalar.muzikcalar

import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SeekBar
import com.example.isletimsistemlericalismasikotlin.R
import com.example.isletimsistemlericalismasikotlin.databinding.FragmentMuzikCalarBinding

class MuzikCalarFragment : Fragment() {

    private lateinit var binding: FragmentMuzikCalarBinding
    private var mediaPlayer: MediaPlayer? = null
    private val songList = arrayListOf(
        R.raw.vidya,
        R.raw.shine,
        R.raw.disfigure
    )
    private val songNames = arrayListOf("Vidya", "Shine", "Disfigure")
    private var currentSongIndex = 0
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMuzikCalarBinding.inflate(inflater, container, false)

        mediaPlayer = MediaPlayer.create(requireActivity(), songList[currentSongIndex])
        binding.musicProgressBar.max = mediaPlayer?.duration ?: 0
        binding.songTitle.text = songNames[currentSongIndex]

        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, songNames)
        binding.lvMuzikCalar.adapter = adapter

        binding.lvMuzikCalar.setOnItemClickListener { _, _, position, _ ->
            mediaPlayer?.stop()
            currentSongIndex = position
            mediaPlayer = MediaPlayer.create(requireActivity(), songList[currentSongIndex])
            mediaPlayer?.start()
            binding.songTitle.text = songNames[currentSongIndex]
            binding.musicProgressBar.max = mediaPlayer?.duration ?: 0
            updateSeekBar()
        }

        binding.buttonPlay.setOnClickListener {
            mediaPlayer?.start()
            updateSeekBar()
        }

        binding.buttonPause.setOnClickListener {
            mediaPlayer?.pause()
        }

        binding.buttonStop.setOnClickListener {
            mediaPlayer?.stop()
            mediaPlayer?.prepareAsync()
            binding.musicProgressBar.progress = 0
        }

        binding.buttonNext.setOnClickListener {
            playNextSong()
        }

        binding.buttonPreview.setOnClickListener {
            playPreviousSong()
        }

        binding.musicProgressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        return binding.root
    }

    private fun playNextSong() {
        mediaPlayer?.stop()
        currentSongIndex = (currentSongIndex + 1) % songList.size
        mediaPlayer = MediaPlayer.create(requireActivity(), songList[currentSongIndex])
        mediaPlayer?.start()
        binding.songTitle.text = songNames[currentSongIndex]
        binding.musicProgressBar.max = mediaPlayer?.duration ?: 0
        updateSeekBar()
    }

    private fun playPreviousSong() {
        mediaPlayer?.stop()
        currentSongIndex = (currentSongIndex - 1 + songList.size) % songList.size
        mediaPlayer = MediaPlayer.create(requireActivity(), songList[currentSongIndex])
        mediaPlayer?.start()
        binding.songTitle.text = songNames[currentSongIndex]
        binding.musicProgressBar.max = mediaPlayer?.duration ?: 0
        updateSeekBar()
    }

    private fun updateSeekBar() {
        Thread {
            while (mediaPlayer?.isPlaying == true) {
                try {
                    Thread.sleep(1000)
                    requireActivity().runOnUiThread {
                        binding.musicProgressBar.progress = mediaPlayer?.currentPosition ?: 0
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
