package com.example.vamsismusicplayer.dataClass

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaMetadataRetriever
import androidx.appcompat.app.AlertDialog
import com.example.vamsismusicplayer.MusicPlayer
import com.example.vamsismusicplayer.R
import com.google.android.material.color.MaterialColors
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

data class Music(val id:String, val title:String, val album:String ,val artist:String, val duration: Long = 0, val path: String,
                 val artUri:String)

class Playlist{
    lateinit var name: String
    lateinit var playlist: ArrayList<Music>
    lateinit var createdBy: String
    lateinit var createdOn: String
}
class MusicPlaylist{
    var ref: ArrayList<Playlist> = ArrayList()
}


fun getImgArt(path: String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}
fun setSongPosition(increment: Boolean){
    if(!MusicPlayer.repeat){
        if(increment)
        {
            if(MusicPlayer.musicListPA.size - 1 == MusicPlayer.songPosition)
                MusicPlayer.songPosition = 0
            else ++MusicPlayer.songPosition
        }else{
            if(MusicPlayer.songPosition == 0)
                MusicPlayer.songPosition = MusicPlayer.musicListPA.size-1
            else --MusicPlayer.songPosition
        }
    }
}
fun exitApplication(){
    if(MusicPlayer.musicService != null){
        MusicPlayer.musicService!!.audioManager.abandonAudioFocus(MusicPlayer.musicService)
        MusicPlayer.musicService!!.stopForeground(true)
        MusicPlayer.musicService!!.mediaPlayer!!.release()
        MusicPlayer.musicService = null}
    exitProcess(1)
}
fun formatDuration(duration: Long):String{
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) -
            minutes* TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
    return String.format("%02d:%02d", minutes, seconds)
}

//fun favouriteChecker(id: String): Int{
//    MusicPlayer.isFavourite = false
//    Favorites.favouriteSongs.forEachIndexed { index, music ->
//        if(id == music.id){
//            MusicPlayer.isFavourite = true
//            return index
//        }
//    }
//    return -1
//}
fun checkPlaylist(playlist: ArrayList<Music>): ArrayList<Music>{
    playlist.forEachIndexed { index, music ->
        val file = File(music.path)
        if(!file.exists())
            playlist.removeAt(index)
    }
    return playlist
}

fun setDialogBtnBackground(context: Context, dialog: AlertDialog){
    //setting button text
    dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(
        MaterialColors.getColor(context, R.attr.dialogTextColor, Color.BLACK)
    )
    dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
        MaterialColors.getColor(context, R.attr.dialogTextColor, Color.BLACK)
    )

    //setting button background
    dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.setBackgroundColor(
        MaterialColors.getColor(context, R.attr.dialogBtnBackground, Color.BLUE)
    )
    dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)?.setBackgroundColor(
        MaterialColors.getColor(context, R.attr.dialogBtnBackground, Color.BLUE)
    )
}

fun getMainColor(img: Bitmap): Int {
    val newImg = Bitmap.createScaledBitmap(img, 1,1 , true)
    val color = newImg.getPixel(0, 0)
    newImg.recycle()
    return color
}