package com.example.vamsismusicplayer.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.vamsismusicplayer.MusicPlayer
import com.example.vamsismusicplayer.R
import com.example.vamsismusicplayer.dataClass.setSongPosition
import com.example.vamsismusicplayer.fragments.NowPlaying
import kotlin.system.exitProcess

class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            //only play next or prev song, when music list contains more than one song
            ApplicationClass.PREVIOUS -> if(MusicPlayer.musicListPA.size > 1) prevNextSong(increment = false, context = context!!)
            ApplicationClass.PLAY -> if(MusicPlayer.isPlaying) pauseMusic() else playMusic()
            ApplicationClass.NEXT -> if(MusicPlayer.musicListPA.size > 1) prevNextSong(increment = true, context = context!!)
            ApplicationClass.EXIT ->{
                MusicPlayer.musicService!!.stopForeground(true)
                MusicPlayer.musicService = null
                exitProcess(1)
                //exitApplication()
            }
        }
    }
    private fun playMusic(){
        MusicPlayer.isPlaying = true
        MusicPlayer.musicService!!.mediaPlayer!!.start()
        MusicPlayer.musicService!!.showNotification(R.drawable.baseline_pause_24)
        MusicPlayer.binding.floatingActionButton2.setImageResource(R.drawable.baseline_pause_24)
        //for handling app crash during notification play - pause btn (While app opened through intent)
        try{ NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.baseline_pause_24) }catch (_: Exception){}
    }

    private fun pauseMusic(){
        MusicPlayer.isPlaying = false
        MusicPlayer.musicService!!.mediaPlayer!!.pause()
        MusicPlayer.musicService!!.showNotification(R.drawable.baseline_play_arrow_24)
        MusicPlayer.binding.floatingActionButton2.setImageResource(R.drawable.baseline_play_arrow_24)
        //for handling app crash during notification play - pause btn (While app opened through intent)
        try{ NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.baseline_play_arrow_24) }catch (_: Exception){}
    }

    private fun prevNextSong(increment: Boolean, context: Context){
        setSongPosition(increment = increment)
        MusicPlayer.musicService!!.createMediaPlayer()
        Glide.with(context)
            .load(MusicPlayer.musicListPA[MusicPlayer.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music).centerCrop())
            .into(MusicPlayer.binding.backgroundImage)
        MusicPlayer.binding.songPlaying.text = MusicPlayer.musicListPA[MusicPlayer.songPosition].title
        Glide.with(context)
            .load(MusicPlayer.musicListPA[MusicPlayer.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music).centerCrop())
            .into(NowPlaying.binding.songImgNP)
        NowPlaying.binding.songNameNP.text = MusicPlayer.musicListPA[MusicPlayer.songPosition].title
        playMusic()
        //MusicPlayer.fIndex = favouriteChecker(MusicPlayer.musicListPA[MusicPlayer.songPosition].id)
        if(MusicPlayer.isFavourite) MusicPlayer.binding.favorite.setImageResource(R.drawable.baseline_favorite_24)
        else MusicPlayer.binding.favorite.setImageResource(R.drawable.baseline_favorite_border_24)
    }
}