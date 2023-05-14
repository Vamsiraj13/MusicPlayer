package com.example.vamsismusicplayer

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.MediaPlayer
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.vamsismusicplayer.dataClass.Music
import com.example.vamsismusicplayer.dataClass.formatDuration
import com.example.vamsismusicplayer.dataClass.getImgArt
import com.example.vamsismusicplayer.dataClass.setSongPosition
import com.example.vamsismusicplayer.databinding.ActivityMusicPlayerBinding
import com.example.vamsismusicplayer.services.MusicService

class MusicPlayer : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {
    companion object {
        lateinit var musicListPA : ArrayList<Music>
        var songPosition: Int = 0
        var isPlaying:Boolean = false
        var musicService: MusicService? = null
        @SuppressLint("StaticFieldLeak")
        val currentThemeNav = arrayOf(R.style.coolPinkNav, R.style.coolBlueNav, R.style.coolPurpleNav, R.style.coolGreenNav,
            R.style.coolBlackNav)
        const val FOREGROUND_PERMISSION_CODE = 1001
        var repeat: Boolean = false
        var min15: Boolean = false
        var min30: Boolean = false
        var min60: Boolean = false
        var nowPlayingId: String = ""
        var isFavourite: Boolean = false
        lateinit var binding: ActivityMusicPlayerBinding
        var fIndex: Int = -1
        lateinit var loudnessEnhancer: LoudnessEnhancer
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMusicPlayerBinding.inflate(layoutInflater)
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        setContentView(binding.root)
        if(intent.data?.scheme.contentEquals("content")){
            songPosition = 0
            val intentService = Intent(this, MusicService::class.java)
            bindService(intentService, this, BIND_AUTO_CREATE)
            startService(intentService)
            musicListPA = ArrayList()
            musicListPA.add(getMusicDetails(intent.data!!))
            Glide.with(this)
                .load(getImgArt(musicListPA[songPosition].path))
                .apply(RequestOptions().placeholder(R.drawable.music).centerCrop())
                .into(binding.backgroundImage)
            binding.songPlaying.text = musicListPA[songPosition].title
        }
        else initializeLayout()

        //service
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        startService(intent)
        binding.floatingActionButton2.setOnClickListener{
            if (!isPlaying){
                play()
            }
            else{
                pause()
            }
        }

        binding.floatingActionButton3.setOnClickListener{
            prevNext(true)
        }
        binding.floatingActionButton4.setOnClickListener{
            prevNext(false)
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser){
                    musicService!!.mediaPlayer!!.seekTo(progress)
                }
            }
        
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
        
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        
        })
        binding.repeat.setOnClickListener{
            if(!repeat){
                repeat = true
                binding.repeat.setImageResource(R.drawable.baseline_repeat_one_24)
            }
            else{
                repeat = false
                binding.repeat.setImageResource(R.drawable.baseline_repeat_24)
            }
        }
        binding.floatingActionButton.setOnClickListener{finish()}

        binding.share.setOnClickListener{
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPA[songPosition].path))
            startActivity(Intent.createChooser(shareIntent, "Sharing Music File!!"))
        }
    }


    private fun getMusicDetails(contentUri: Uri): Music{
        var cursor: Cursor? = null
        try {
            val projection = arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION)
            cursor = this.contentResolver.query(contentUri, projection, null, null, null)
            val dataColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            cursor!!.moveToFirst()
            val path = dataColumn?.let { cursor.getString(it) }
            val duration = durationColumn?.let { cursor.getLong(it) }!!
            return Music(id = "Unknown", title = path.toString(), album = "Unknown", artist = "Unknown", duration = duration,
                artUri = "Unknown", path = path.toString())
        }finally {
            cursor?.close()
        }
    }
    private fun setLayout(){

        Glide.with(this)
            .load(musicListPA[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music).centerCrop())
            .into(binding.backgroundImage)
        binding.songPlaying.text = musicListPA[songPosition].title
        binding.author.text = musicListPA[songPosition].artist
       
    }

    private fun createMediaPlayer(){
        try {
            if (musicService!!.mediaPlayer == null) {
                musicService!!.mediaPlayer = MediaPlayer()
            }
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
            musicService!!.mediaPlayer!!.prepare()

            binding.start.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.timeStamp.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            binding.seekBar.progress = 0
            binding.seekBar.max = musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
            nowPlayingId = musicListPA[songPosition].id
            play()
        }
        catch (e:java.lang.Exception){return}
    }
    private fun initializeLayout(){
        songPosition = intent.getIntExtra("index", 0)
        when(intent.getStringExtra("class")){
            "MusicAdapterSearch"-> initServiceAndPlaylist(MainActivity.musicListSearch, shuffle = false)
            "MusicAdapter" -> initServiceAndPlaylist(MainActivity.songList, shuffle = false)
            //"FavouriteAdapter"-> initServiceAndPlaylist(Favorites.favouriteSongs, shuffle = false)
            "MainActivity"-> initServiceAndPlaylist(MainActivity.songList, shuffle = true)
            "NowPlaying"->{
                setLayout()
                binding.start.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.timeStamp.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seekBar.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekBar.max = musicService!!.mediaPlayer!!.duration
                if(isPlaying) binding.floatingActionButton2.setImageResource(R.drawable.baseline_pause_24)
                else binding.floatingActionButton2.setImageResource(R.drawable.baseline_play_arrow_24)
            }
        }
    }

    private fun play(){
        binding.floatingActionButton2.setImageResource(R.drawable.baseline_pause_24)
        isPlaying = true
        musicService!!.mediaPlayer!!.start()
    }
    private fun pause(){
        binding.floatingActionButton2.setImageResource(R.drawable.baseline_play_arrow_24)
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()
    }

    private fun prevNext(boolean: Boolean){
        if(boolean){
            checkSongPosition(boolean=true)
            setLayout()
            createMediaPlayer()
        }
        else{
            checkSongPosition(boolean=false)
            setLayout()
            createMediaPlayer()
        }
    }
    private fun checkSongPosition(boolean: Boolean){
        if(boolean){
            if(songPosition == musicListPA.size-1){
                songPosition = 0
            }
            else ++songPosition
        }else{
            if(songPosition==0){
                songPosition = musicListPA.size-1
            }
            else --songPosition
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        createMediaPlayer()
        //ActivityCompat.requestPermissions(this, arrayOf(FOREGROUND_SERVICE), FOREGROUND_PERMISSION_CODE)
        //musicService!!.showNotification(R.drawable.baseline_pause_24)
        musicService!!.createMediaPlayer()
        musicService!!.seekBarSetup()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    override fun onCompletion(mp: MediaPlayer?) {
        setSongPosition(increment = true)
        createMediaPlayer()
        setLayout()

        //for refreshing now playing image & text on song completion
        //NowPlaying.binding.songNameNP.isSelected = true
        //Glide.with(applicationContext)
        //    .load(musicListPA[songPosition].artUri)
        //    .apply(RequestOptions().placeholder(R.drawable.music).centerCrop())
        //    .into(NowPlaying.binding.songImgNP)
        //NowPlaying.binding.songNameNP.text = musicListPA[songPosition].title
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == FOREGROUND_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                musicService!!.showNotification(R.drawable.baseline_pause_24)
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initServiceAndPlaylist(playlist: ArrayList<Music>, shuffle: Boolean, playNext: Boolean = false){
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        startService(intent)
        musicListPA = ArrayList()
        musicListPA.addAll(playlist)
        if(shuffle) musicListPA.shuffle()
        setLayout()
    }
}