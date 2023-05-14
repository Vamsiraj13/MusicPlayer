package com.example.vamsismusicplayer.services

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import com.example.vamsismusicplayer.MainActivity
import com.example.vamsismusicplayer.MusicPlayer
import com.example.vamsismusicplayer.R
import com.example.vamsismusicplayer.dataClass.formatDuration
import com.example.vamsismusicplayer.fragments.NowPlaying

class MusicService: Service(), AudioManager.OnAudioFocusChangeListener {
    private var myBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null
    private lateinit var mediaSession : MediaSessionCompat
    private lateinit var runnable: Runnable
    lateinit var audioManager: AudioManager

    override fun onBind(intent: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "My Music")
        return myBinder
    }

    inner class MyBinder: Binder(){
        fun currentService(): MusicService {
            return this@MusicService
        }
    }
    @SuppressLint("UnspecifiedImmutableFlag")
    fun showNotification(playPauseBtn: Int){
        val intent = Intent(baseContext, MainActivity::class.java)
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val contentIntent = PendingIntent.getActivity(this, 0, intent, flag)

        val prevIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.PREVIOUS)
        val prevPendingIntent = PendingIntent.getBroadcast(baseContext, 0, prevIntent, flag)

        val playIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(baseContext, 0, playIntent, flag)

        val nextIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(baseContext, 0, nextIntent, flag)

        val exitIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.EXIT)
        val exitPendingIntent = PendingIntent.getBroadcast(baseContext, 0, exitIntent, flag)

        val notification = NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
            .setContentIntent(contentIntent)
            .setContentTitle(MusicPlayer.musicListPA[MusicPlayer.songPosition].title)
            .setContentText(MusicPlayer.musicListPA[MusicPlayer.songPosition].artist)
            .setSmallIcon(R.drawable.music)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.music))
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.baseline_skip_previous_24, "Previous", prevPendingIntent)
            .addAction(R.drawable.baseline_play_arrow_24, "Play", playPendingIntent)
            .addAction(R.drawable.baseline_skip_next_24, "Next", nextPendingIntent)
            .addAction(R.drawable.baseline_exit_to_app_24, "Exit", exitPendingIntent)
            .build()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            val playbackSpeed = if(MusicPlayer.isPlaying) 1F else 0F
            mediaSession.setMetadata(
                MediaMetadataCompat.Builder()
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer!!.duration.toLong())
                .build())
            val playBackState = PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer!!.currentPosition.toLong(), playbackSpeed)
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                .build()
            mediaSession.setPlaybackState(playBackState)
            mediaSession.setCallback(object: MediaSessionCompat.Callback(){

                //called when headphones buttons are pressed
                //currently only pause or play music on button click
                override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
                    if(MusicPlayer.isPlaying){
                        //pause music
                        MusicPlayer.binding.floatingActionButton2.setImageResource(R.drawable.baseline_play_arrow_24)
                        //NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.baseline_play_arrow_24)
                        MusicPlayer.isPlaying = false
                        mediaPlayer!!.pause()
                        showNotification(R.drawable.baseline_play_arrow_24)
                    }else{
                        //play music
                        MusicPlayer.binding.floatingActionButton2.setImageResource(R.drawable.baseline_pause_24)
                        //NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.baseline_pause_24)
                        MusicPlayer.isPlaying = true
                        mediaPlayer!!.start()
                        showNotification(R.drawable.baseline_pause_24)
                    }
                    return super.onMediaButtonEvent(mediaButtonEvent)
                }
                override fun onSeekTo(pos: Long) {
                    super.onSeekTo(pos)
                    mediaPlayer!!.seekTo(pos.toInt())
                    val playBackStateNew = PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer!!.currentPosition.toLong(), playbackSpeed)
                        .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                        .build()
                    mediaSession.setPlaybackState(playBackStateNew)
                }
            })
        }

        startForeground(11, notification)
    }

    fun createMediaPlayer(){
        try {
            if (mediaPlayer == null) mediaPlayer = MediaPlayer()
            mediaPlayer!!.reset()
            mediaPlayer!!.setDataSource(MusicPlayer.musicListPA[MusicPlayer.songPosition].path)
            mediaPlayer!!.prepare()
            mediaPlayer!!.start()
            MusicPlayer.binding.floatingActionButton2.setImageResource(R.drawable.baseline_pause_24)
            //showNotification(R.drawable.baseline_pause_24)
            MusicPlayer.binding.start.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            MusicPlayer.binding.timeStamp.text = formatDuration(mediaPlayer!!.duration.toLong())
            MusicPlayer.binding.seekBar.progress = 0
            MusicPlayer.binding.seekBar.max = mediaPlayer!!.duration
            MusicPlayer.nowPlayingId = MusicPlayer.musicListPA[MusicPlayer.songPosition].id
        }catch (e: Exception){return}
    }

    fun seekBarSetup(){
        runnable = Runnable {
            MusicPlayer.binding.start.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            MusicPlayer.binding.seekBar.progress = mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        if(focusChange <= 0){
            //pause music
            MusicPlayer.binding.floatingActionButton2.setImageResource(R.drawable.baseline_play_arrow_24)
            NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.baseline_play_arrow_24)
            MusicPlayer.isPlaying = false
            mediaPlayer!!.pause()
            //showNotification(R.drawable.baseline_play_arrow_24)

        }
    }
}