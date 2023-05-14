package com.example.vamsismusicplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.vamsismusicplayer.databinding.ActivityFavoritesBinding

class Favorites : AppCompatActivity() {
    private lateinit var binding: ActivityFavoritesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}