package com.example.vamsismusicplayer.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.vamsismusicplayer.MainActivity
import com.example.vamsismusicplayer.MusicPlayer
import com.example.vamsismusicplayer.R
import com.example.vamsismusicplayer.dataClass.setSongPosition
import com.example.vamsismusicplayer.databinding.FragmentNowPlayingBinding

class NowPlaying  : Fragment() {

    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: FragmentNowPlayingBinding
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        requireContext().theme.applyStyle(MainActivity.currentTheme[MainActivity.themeIndex], true)
        val view = inflater.inflate(R.layout.fragment_now_playing, container, false)
        binding = FragmentNowPlayingBinding.bind(view)
        binding.root.visibility = View.INVISIBLE
        binding.playPauseBtnNP.setOnClickListener {
            if(MusicPlayer.isPlaying) pauseMusic() else playMusic()
        }
        binding.nextBtnNP.setOnClickListener {
            setSongPosition(increment = true)
            MusicPlayer.musicService!!.createMediaPlayer()
            Glide.with(requireContext())
                .load(MusicPlayer.musicListPA[MusicPlayer.songPosition].artUri)
                .apply(RequestOptions().placeholder(R.drawable.music).centerCrop())
                .into(binding.songImgNP)
            binding.songNameNP.text = MusicPlayer.musicListPA[MusicPlayer.songPosition].title
            MusicPlayer.musicService!!.showNotification(R.drawable.baseline_pause_24)
            playMusic()
        }
        binding.root.setOnClickListener {
            val intent = Intent(requireContext(), MusicPlayer::class.java)
            intent.putExtra("index", MusicPlayer.songPosition)
            intent.putExtra("class", "NowPlaying")
            ContextCompat.startActivity(requireContext(), intent, null)
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        if(MusicPlayer.musicService != null){
            binding.root.visibility = View.VISIBLE
            binding.songNameNP.isSelected = true
            Glide.with(requireContext())
                .load(MusicPlayer.musicListPA[MusicPlayer.songPosition].artUri)
                .apply(RequestOptions().placeholder(R.drawable.music).centerCrop())
                .into(binding.songImgNP)
            binding.songNameNP.text = MusicPlayer.musicListPA[MusicPlayer.songPosition].title
            if(MusicPlayer.isPlaying) binding.playPauseBtnNP.setIconResource(R.drawable.baseline_pause_24)
            else binding.playPauseBtnNP.setIconResource(R.drawable.baseline_play_arrow_24)
        }
    }

    private fun playMusic(){
        MusicPlayer.isPlaying = true
        MusicPlayer.musicService!!.mediaPlayer!!.start()
        binding.playPauseBtnNP.setIconResource(R.drawable.baseline_pause_24)
        MusicPlayer.musicService!!.showNotification(R.drawable.baseline_pause_24)
    }
    private fun pauseMusic(){
        MusicPlayer.isPlaying = false
        MusicPlayer.musicService!!.mediaPlayer!!.pause()
        binding.playPauseBtnNP.setIconResource(R.drawable.baseline_play_arrow_24)
        MusicPlayer.musicService!!.showNotification(R.drawable.baseline_play_arrow_24)
    }
}