package com.example.vamsismusicplayer

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.example.vamsismusicplayer.databinding.ActivityPlaylistBinding

class Playlist : AppCompatActivity() {
    private lateinit var binding: ActivityPlaylistBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}