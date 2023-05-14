package com.example.vamsismusicplayer.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.vamsismusicplayer.MainActivity
import com.example.vamsismusicplayer.MusicPlayer
import com.example.vamsismusicplayer.R
import com.example.vamsismusicplayer.dataClass.Music
import com.example.vamsismusicplayer.databinding.MusicItemBinding
import java.util.concurrent.TimeUnit

class MusicAdapter(private val context: Context, private var songList: ArrayList<Music>): RecyclerView.Adapter<MusicAdapter.MyViewHolder>(){

    class MyViewHolder(binding: MusicItemBinding): RecyclerView.ViewHolder(binding.root){
        val title = binding.songName
        val album = binding.songAlbumMV
        val duration = binding.songDuration
        val image = binding.image
        val root = binding.root
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(MusicItemBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.title.text = songList[position].title
        holder.album.text = songList[position].album
        holder.duration.text = formatDuration(songList[position].duration)
        Glide.with(context)
            .load(songList[position].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music).centerCrop())
            .into(holder.image)
        holder.root.setOnClickListener{
            when{
                MainActivity.search -> sendIntent(ref = "MusicAdapterSearch", pos = position)
                songList[position].id == MusicPlayer.nowPlayingId ->
                    sendIntent(ref = "NowPlaying", pos = MusicPlayer.songPosition)
                else->sendIntent(ref="MusicAdapter", pos = position) }
        }
    }

    override fun getItemCount(): Int {
        return songList.size
    }
    fun updateMusicList(searchList : ArrayList<Music>){
        songList = ArrayList()
        songList.addAll(searchList)
        notifyDataSetChanged()
    }

    private fun formatDuration(duration: Long):String{
        val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
        val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) -
                minutes* TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
        return String.format("%02d:%02d", minutes, seconds)
    }
    private fun sendIntent(ref: String, pos: Int){
        val intent = Intent(context, MusicPlayer::class.java)
        intent.putExtra("index", pos)
        intent.putExtra("class", ref)
        ContextCompat.startActivity(context, intent, null)
    }
}